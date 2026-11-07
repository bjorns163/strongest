package com.strongest.app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.strongest.app.MainActivity
import com.strongest.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "workout_active_channel_v2"
private const val NOTIFICATION_ID = 2001

class WorkoutForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob())
    private var collectorJob: Job? = null

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // The rest-timer buttons are handled here rather than by the workout screen, so they keep
    // working after that screen is gone. Set and finish actions still need the screen.
    private val timerActionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val adjustment = (WorkoutNotificationBus.state.value as? WorkoutNotificationState.Resting)
                ?.adjustmentSeconds ?: 0
            when (intent.action) {
                ACTION_TIMER_ADD -> RestTimer.adjust(context, adjustment)
                ACTION_TIMER_SUBTRACT -> RestTimer.adjust(context, -adjustment)
                ACTION_SKIP_REST -> RestTimer.cancel()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val filter = IntentFilter().apply {
            addAction(ACTION_TIMER_ADD)
            addAction(ACTION_TIMER_SUBTRACT)
            addAction(ACTION_SKIP_REST)
        }
        ContextCompat.registerReceiver(this, timerActionsReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.active_workout),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initial = buildNotification(
            WorkoutNotificationBus.state.value,
            WorkoutNotificationBus.attached.value,
            RestTimer.state.value
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    initial,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, initial)
            }
        } catch (_: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException (Android 12+): the app is in the
            // background, e.g. battery-restricted. Crash reports showed this killing the app;
            // without the foreground slot there is nothing to show, so just stop.
            stopSelf()
            return START_NOT_STICKY
        }

        if (collectorJob == null) {
            collectorJob = scope.launch {
                combine(
                    WorkoutNotificationBus.state,
                    WorkoutNotificationBus.attached,
                    RestTimer.state,
                    ::Triple
                ).collectLatest { (state, attached, rest) ->
                    if (state == null) {
                        stopSelf()
                    } else {
                        notificationManager.notify(NOTIFICATION_ID, buildNotification(state, attached, rest))
                    }
                }
            }
        }

        // Not sticky: after the process dies the workout state lives only in the database, so a
        // system restart would have nothing to show, and from the background Android 12+ refuses
        // to promote it to the foreground anyway.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        try { unregisterReceiver(timerActionsReceiver) } catch (_: Exception) {}
        scope.cancel()
        collectorJob = null
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * [attached] is false once the workout screen is gone: the set and finish actions are left out
     * (nothing would handle them) and tapping the notification reopens the app instead.
     */
    private fun buildNotification(
        state: WorkoutNotificationState?,
        attached: Boolean,
        rest: RestTimerState?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPending = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dumbbell)
            .setContentIntent(openAppPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setRequestPromotedOngoing(true)
        
        if (state != null && state.workoutStartTime > 0L) {
            builder.setWhen(state.workoutStartTime).setUsesChronometer(true)
        } else {
            builder.setUsesChronometer(false)
        }

        when (state) {
            null -> {
                builder.setContentTitle(getString(R.string.workout_default_name))
                    .setContentText(getString(R.string.notification_starting))
            }
            is WorkoutNotificationState.WaitingForExercises -> {
                builder.setContentTitle(state.workoutName)
                    .setContentText(getString(R.string.notification_tap_to_add))
            }
            is WorkoutNotificationState.SetReady -> {
                val weightText = formatWeightForDisplay(state.weightKg, state.weightUnit)
                val unitLabel = weightUnitLabel(state.weightUnit)
                builder.setContentTitle(state.workoutName)
                    .setContentText(
                        getString(
                            R.string.notification_set_ready,
                            state.exerciseName,
                            state.setNumber,
                            state.totalSetsInExercise,
                            "$weightText $unitLabel",
                            state.reps
                        )
                    )
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            resources.getQuantityString(
                                R.plurals.notification_set_ready_long,
                                state.reps,
                                state.exerciseName,
                                state.setNumber,
                                state.totalSetsInExercise,
                                "$weightText $unitLabel",
                                state.reps
                            )
                        )
                    )
                if (attached) {
                    builder.addAction(
                        0,
                        getString(R.string.notification_complete_set),
                        broadcastPending(REQ_COMPLETE, ACTION_COMPLETE_SET)
                    )
                }
            }
            is WorkoutNotificationState.Resting -> {
                val nextWeightText = formatWeightForDisplay(state.nextWeightKg, state.weightUnit)
                val unitLabel = weightUnitLabel(state.weightUnit)
                val nextText = getString(
                    R.string.notification_next,
                    state.nextExerciseName,
                    state.nextSetNumber,
                    "$nextWeightText $unitLabel",
                    state.nextReps
                )
                if (rest == null) {
                    // The rest ran out while the workout screen was closed, so nothing has
                    // published the next set yet: show what's next until the workout is reopened.
                    builder.setContentTitle(state.workoutName).setContentText(nextText)
                    return builder.build()
                }
                val mm = rest.remainingSeconds / 60
                val ss = rest.remainingSeconds % 60
                val timeText = String.format("%02d:%02d", mm, ss)
                val progress =
                    if (rest.totalSeconds > 0) rest.remainingSeconds * 100 / rest.totalSeconds else 0
                builder.setContentTitle(getString(R.string.notification_rest, timeText))
                    .setContentText(nextText)
                    .setProgress(100, progress, false)
                    .addAction(
                        0,
                        "-" + getString(R.string.seconds_short, state.adjustmentSeconds),
                        broadcastPending(REQ_MINUS, ACTION_TIMER_SUBTRACT)
                    )
                    .addAction(
                        0,
                        getString(R.string.skip),
                        broadcastPending(REQ_SKIP, ACTION_SKIP_REST)
                    )
                    .addAction(
                        0,
                        "+" + getString(R.string.seconds_short, state.adjustmentSeconds),
                        broadcastPending(REQ_PLUS, ACTION_TIMER_ADD)
                    )
                builder.setShortCriticalText(timeText)
            }
            is WorkoutNotificationState.AllDone -> {
                builder.setContentTitle(state.workoutName)
                    .setContentText(getString(R.string.notification_all_done))
                if (attached) {
                    builder.addAction(
                        0,
                        getString(R.string.notification_complete_workout),
                        broadcastPending(REQ_FINISH, ACTION_FINISH_WORKOUT)
                    )
                }
            }
        }

        return builder.build()
    }

    private fun broadcastPending(requestCode: Int, action: String): PendingIntent {
        val intent = Intent(action).apply { setPackage(packageName) }
        return PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    companion object {
        private const val REQ_COMPLETE = 11
        private const val REQ_SKIP = 12
        private const val REQ_PLUS = 13
        private const val REQ_MINUS = 14
        private const val REQ_FINISH = 15

        fun start(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java)
            try {
                context.startForegroundService(intent)
            } catch (_: IllegalStateException) {
                // Android 12+ refuses when the app is already in the background (a workout load
                // can finish after the user left). The notification is skipped, the workout isn't.
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkoutForegroundService::class.java))
        }
    }
}
