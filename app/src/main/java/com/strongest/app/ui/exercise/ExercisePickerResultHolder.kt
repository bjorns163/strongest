package com.strongest.app.ui.exercise

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cross-screen handoff for the exercise picker result.
 *
 * Using a [StateFlow] (rather than a plain `var`) means consumers can `collect`/`LaunchedEffect`
 * keyed on the value, and changes published from the picker actually trigger the consuming
 * screen — previously `var result` was captured by value at composition time, so updates were
 * only observed when the consuming composable happened to recompose for other reasons.
 *
 * [isReplaceMode] and [replacingExerciseId] are read together with the result. Every way into
 * the picker sets them through [startAdd] or [startReplace]: they used to be cleared only when a
 * result came back, so backing out of a Replace left them set and the next "Add Exercise"
 * silently replaced an exercise instead.
 */
@Singleton
class ExercisePickerResultHolder @Inject constructor() {
    private val _result = MutableStateFlow<List<Long>?>(null)
    val result: StateFlow<List<Long>?> = _result.asStateFlow()

    var isReplaceMode: Boolean = false
    var replacingExerciseId: Long? = null

    /** Call before opening the picker to add exercises. */
    fun startAdd() {
        clearReplaceMode()
        _result.value = null
    }

    /** Call before opening the picker to replace the exercise with id [exerciseId] (screen-specific id). */
    fun startReplace(exerciseId: Long) {
        isReplaceMode = true
        replacingExerciseId = exerciseId
        _result.value = null
    }

    fun publish(ids: List<Long>) {
        _result.value = ids
    }

    fun consume(): List<Long>? {
        val ids = _result.value
        _result.value = null
        return ids
    }

    fun clearReplaceMode() {
        isReplaceMode = false
        replacingExerciseId = null
    }
}
