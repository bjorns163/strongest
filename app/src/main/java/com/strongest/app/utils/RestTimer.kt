package com.strongest.app.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.net.toUri
import com.strongest.app.data.repository.SILENT_SOUND
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A running rest countdown for one workout. [endsAt] is on the [SystemClock.elapsedRealtime] timebase. */
data class RestTimerState(
    val workoutId: Long,
    val setId: Long?,
    val endsAt: Long,
    val totalSeconds: Int,
    val remainingSeconds: Int
)

/**
 * The app-wide rest timer: counts down, then plays the alert.
 *
 * It lives outside the workout screen on purpose. That screen's ViewModel is destroyed when the
 * user navigates back or swipes the app away, which used to cancel the rest silently. A partial
 * wake lock is held only while a rest runs, so the countdown keeps ticking and the alert fires on
 * time with the screen off instead of whenever the CPU next wakes up.
 */
object RestTimer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow<RestTimerState?>(null)
    val state: StateFlow<RestTimerState?> = _state.asStateFlow()

    private var tickJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var alertSoundUri: String? = null

    /** Clock source; swapped in tests so the countdown can run on virtual time. */
    internal var now: () -> Long = SystemClock::elapsedRealtime

    /** Starts (or restarts) a rest. [soundUri] follows the settings value: null is the default sound. */
    fun start(context: Context, workoutId: Long, setId: Long?, seconds: Int, soundUri: String?) {
        val appContext = context.applicationContext
        alertSoundUri = soundUri
        val endsAt = now() + seconds * 1000L
        _state.value = RestTimerState(workoutId, setId, endsAt, seconds, seconds)
        holdWakeLock(appContext, seconds * 1000L)
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                val current = _state.value ?: return@launch
                val remaining = ((current.endsAt - now()) / 1000).toInt()
                if (remaining <= 0) {
                    _state.value = null
                    playAlert(appContext, alertSoundUri)
                    return@launch
                }
                _state.value = current.copy(remainingSeconds = remaining)
            }
        }
    }

    /** Moves the end of the running rest by [seconds]; the total grows if the rest now runs longer. */
    fun adjust(context: Context, seconds: Int) {
        val current = _state.value ?: return
        val endsAt = current.endsAt + seconds * 1000L
        val remaining = ((endsAt - now()) / 1000).toInt().coerceAtLeast(0)
        _state.value = current.copy(
            endsAt = endsAt,
            remainingSeconds = remaining,
            totalSeconds = maxOf(current.totalSeconds, remaining)
        )
        holdWakeLock(context.applicationContext, remaining * 1000L)
    }

    /** Stops the rest without an alert (skipped, or the workout ended). */
    fun cancel() {
        tickJob?.cancel()
        tickJob = null
        _state.value = null
        releaseWakeLock()
    }

    /** Cancels only if the running rest belongs to [workoutId]. */
    fun cancelFor(workoutId: Long?) {
        if (workoutId != null && _state.value?.workoutId == workoutId) cancel()
    }

    private fun holdWakeLock(context: Context, durationMs: Long) {
        val lock = wakeLock ?: context.getSystemService(PowerManager::class.java)
            ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "strongest:rest_timer")
            ?.apply { setReferenceCounted(false) }
            ?.also { wakeLock = it }
        // The timeout is a safety net: the lock is released explicitly once the alert has played.
        lock?.acquire(durationMs + ALERT_GRACE_MS)
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
    }

    /** After an alert: a new rest may already have started and needs the lock to stay held. */
    private fun releaseWakeLockIfIdle() {
        if (_state.value == null) releaseWakeLock()
    }

    private fun playAlert(context: Context, soundUri: String?) {
        vibrate(context)
        if (soundUri == SILENT_SOUND) {
            releaseWakeLockIfIdle()
            return
        }
        val uri = soundUri?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (uri == null) {
            releaseWakeLockIfIdle()
            return
        }
        // Media rather than notification usage: plays in silent/vibrate mode and through Do Not
        // Disturb (media is allowed by default), follows the media volume and goes to headphones.
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val audioManager = context.getSystemService(AudioManager::class.java)
        // Transient-may-duck: music playing in another app is briefly lowered, not paused.
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .build()
        audioManager?.requestAudioFocus(focusRequest)
        val player = MediaPlayer()
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            player.release()
            audioManager?.abandonAudioFocusRequest(focusRequest)
            releaseWakeLockIfIdle()
        }
        try {
            player.setAudioAttributes(attributes)
            player.setDataSource(context, uri)
            player.setOnPreparedListener { it.start() }
            player.setOnCompletionListener { finish() }
            player.setOnErrorListener { _, _, _ -> finish(); true }
            player.prepareAsync()
        } catch (_: Exception) {
            finish()
        }
    }

    private fun vibrate(context: Context) {
        try {
            val effect = VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 200), -1)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
            } ?: return
            // Alarm usage so the buzz still comes through silent mode and Do Not Disturb.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(
                    effect,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
            }
        } catch (_: Exception) {
        }
    }

    private const val ALERT_GRACE_MS = 15_000L
}
