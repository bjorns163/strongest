package com.strongest.app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.strongest.app.MainActivity
import com.strongest.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "workout_active_channel_v2"
private const val CHANNEL_NAME = "Active Workout"
private const val NOTIFICATION_ID = 2001

class WorkoutForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob())
    private var collectorJob: Job? = null

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Persistent notification during an active workout"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initial = buildNotification(WorkoutNotificationBus.state.value)
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

        if (collectorJob == null) {
            collectorJob = scope.launch {
                WorkoutNotificationBus.state.collectLatest { state ->
                    if (state == null) {
                        stopSelf()
                    } else {
                        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        collectorJob = null
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(state: WorkoutNotificationState?): Notification {
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

        if (state != null && state.workoutStartTime > 0L) {
            builder.setWhen(state.workoutStartTime).setUsesChronometer(true)
        } else {
            builder.setUsesChronometer(false)
        }

        when (state) {
            null -> {
                builder.setContentTitle("Workout").setContentText("Starting…")
            }
            is WorkoutNotificationState.WaitingForExercises -> {
                builder.setContentTitle(state.workoutName)
                    .setContentText("Tap to add exercises")
            }
            is WorkoutNotificationState.SetReady -> {
                val weightText = formatWeightForDisplay(state.weightKg, state.weightUnit)
                val unitLabel = weightUnitLabel(state.weightUnit)
                builder.setContentTitle(state.workoutName)
                    .setContentText(
                        "${state.exerciseName} • Set ${state.setNumber}/${state.totalSetsInExercise} • ${weightText} ${unitLabel} × ${state.reps}"
                    )
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            "${state.exerciseName}\nSet ${state.setNumber} of ${state.totalSetsInExercise} • ${weightText} ${unitLabel} × ${state.reps} reps"
                        )
                    )
                    .addAction(
                        0,
                        "Complete set",
                        broadcastPending(REQ_COMPLETE, ACTION_COMPLETE_SET)
                    )
            }
            is WorkoutNotificationState.Resting -> {
                val mm = state.remainingSeconds / 60
                val ss = state.remainingSeconds % 60
                val timeText = String.format("%02d:%02d", mm, ss)
                val nextWeightText = formatWeightForDisplay(state.nextWeightKg, state.weightUnit)
                val unitLabel = weightUnitLabel(state.weightUnit)
                val progress =
                    if (state.totalSeconds > 0) state.remainingSeconds * 100 / state.totalSeconds else 0
                builder.setContentTitle("Rest • $timeText")
                    .setContentText(
                        "Next: ${state.nextExerciseName} • Set ${state.nextSetNumber} • ${nextWeightText} ${unitLabel} × ${state.nextReps}"
                    )
                    .setProgress(100, progress, false)
                    .addAction(
                        0,
                        "-${state.adjustmentSeconds}s",
                        broadcastPending(REQ_MINUS, ACTION_TIMER_SUBTRACT)
                    )
                    .addAction(
                        0,
                        "Skip",
                        broadcastPending(REQ_SKIP, ACTION_SKIP_REST)
                    )
                    .addAction(
                        0,
                        "+${state.adjustmentSeconds}s",
                        broadcastPending(REQ_PLUS, ACTION_TIMER_ADD)
                    )
            }
            is WorkoutNotificationState.AllDone -> {
                builder.setContentTitle(state.workoutName)
                    .setContentText("All sets done — tap finish to save")
                    .addAction(
                        0,
                        "Complete workout",
                        broadcastPending(REQ_FINISH, ACTION_FINISH_WORKOUT)
                    )
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WorkoutForegroundService::class.java))
        }
    }
}
