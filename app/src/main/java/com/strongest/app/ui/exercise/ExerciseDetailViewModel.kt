package com.strongest.app.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.db.ExerciseHistoryEntry
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseDetailState(
    val exercise: Exercise? = null,
    val history: List<ExerciseHistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val totalSets: Int = 0,
    val totalWorkouts: Int = 0,
    val maxWeight: Float = 0f,
    val noteText: String = "",
    val rpeTrackingEnabled: Boolean = false,
    val warmUpSetCount: Int = 3
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseDetailState())
    val state: StateFlow<ExerciseDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _state.update { it.copy(rpeTrackingEnabled = settings.rpeTrackingEnabled) }
            }
        }
    }

    fun loadExercise(exerciseId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val exercise = repository.getExerciseById(exerciseId)
            val history = repository.getExerciseHistory(exerciseId)
            val note = repository.getNote(exerciseId)
            val settings = repository.getExerciseSettings(exerciseId)

            val totalSets = history.size
            val totalWorkouts = history.groupBy { it.workoutDate }.size
            val workingSets = history.filter { it.setType != com.strongest.app.data.model.SetType.WARM_UP }
            val maxWeight = workingSets.maxOfOrNull { it.weightKg }
                ?: history.maxOfOrNull { it.weightKg }
                ?: 0f

            _state.update {
                it.copy(
                    exercise = exercise,
                    history = history,
                    isLoading = false,
                    totalSets = totalSets,
                    totalWorkouts = totalWorkouts,
                    maxWeight = maxWeight,
                    noteText = note?.noteText ?: "",
                    warmUpSetCount = settings?.warmUpSetCount ?: 3
                )
            }
        }
    }

    fun setWarmUpSetCount(count: Int) {
        _state.update { it.copy(warmUpSetCount = count) }
        val exerciseId = _state.value.exercise?.id ?: return
        viewModelScope.launch {
            repository.saveWarmUpSetCount(exerciseId, count)
        }
    }

    fun updateExercise(
        name: String,
        muscleGroup: MuscleGroup,
        equipment: Equipment,
        type: ExerciseType,
        instructions: String
    ) {
        val exercise = _state.value.exercise ?: return
        viewModelScope.launch {
            val updated = exercise.copy(
                name = name.trim(),
                muscleGroup = muscleGroup,
                equipment = equipment,
                type = type,
                instructions = instructions.trim()
            )
            repository.updateExercise(updated)
            _state.update { it.copy(exercise = updated) }
        }
    }

    fun deleteExercise(onDeleted: () -> Unit) {
        val exercise = _state.value.exercise ?: return
        if (!exercise.isCustom) return
        viewModelScope.launch {
            repository.deleteCustomExercise(exercise)
            onDeleted()
        }
    }

    fun saveNote(noteText: String) {
        val exerciseId = _state.value.exercise?.id ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.upsertNote(ExerciseNote(exerciseId = exerciseId, noteText = noteText, updatedAt = now))
            _state.update { it.copy(noteText = noteText) }
        }
    }
}
