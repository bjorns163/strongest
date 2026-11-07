package com.strongest.app.utils

import com.strongest.app.data.repository.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val ACTION_COMPLETE_SET = "com.strongest.app.COMPLETE_SET"
const val ACTION_SKIP_REST = "com.strongest.app.SKIP_REST"
const val ACTION_FINISH_WORKOUT = "com.strongest.app.FINISH_WORKOUT"
const val ACTION_TIMER_ADD = "com.strongest.app.TIMER_ADD"
const val ACTION_TIMER_SUBTRACT = "com.strongest.app.TIMER_SUBTRACT"

sealed interface WorkoutNotificationState {
    val workoutName: String
    val workoutStartTime: Long
    val weightUnit: WeightUnit

    data class WaitingForExercises(
        override val workoutName: String,
        override val workoutStartTime: Long,
        override val weightUnit: WeightUnit = WeightUnit.KG
    ) : WorkoutNotificationState

    data class SetReady(
        override val workoutName: String,
        override val workoutStartTime: Long,
        val exerciseName: String,
        val setNumber: Int,
        val totalSetsInExercise: Int,
        val weightKg: Float,
        val reps: Int,
        override val weightUnit: WeightUnit = WeightUnit.KG
    ) : WorkoutNotificationState

    data class Resting(
        override val workoutName: String,
        override val workoutStartTime: Long,
        val nextExerciseName: String,
        val nextSetNumber: Int,
        val nextWeightKg: Float,
        val nextReps: Int,
        // The countdown itself comes from RestTimer, which keeps running without the screen.
        val adjustmentSeconds: Int,
        override val weightUnit: WeightUnit = WeightUnit.KG
    ) : WorkoutNotificationState

    data class AllDone(
        override val workoutName: String,
        override val workoutStartTime: Long,
        override val weightUnit: WeightUnit = WeightUnit.KG
    ) : WorkoutNotificationState
}

object WorkoutNotificationBus {
    private val _state = MutableStateFlow<WorkoutNotificationState?>(null)
    val state: StateFlow<WorkoutNotificationState?> = _state.asStateFlow()

    private val _attached = MutableStateFlow(false)

    /**
     * True while the workout screen is alive to handle "Complete set" and "Finish". After the user
     * leaves it (back, or swiping the app away) the notification stays up with the rest timer,
     * but those actions have nothing to act on until the workout is reopened.
     */
    val attached: StateFlow<Boolean> = _attached.asStateFlow()

    fun publish(state: WorkoutNotificationState) {
        _state.value = state
        _attached.value = true
    }

    fun detach() {
        _attached.value = false
    }

    fun clear() {
        _state.value = null
        _attached.value = false
    }
}
