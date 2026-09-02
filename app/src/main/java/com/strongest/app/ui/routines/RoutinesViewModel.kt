package com.strongest.app.ui.routines

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.db.RoutineWithExercisesAndSets
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.utils.parseSharedRoutine
import com.strongest.app.utils.toJson
import com.strongest.app.utils.toSharedRoutine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val routines: StateFlow<List<Routine>> = repository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val routineGroups: StateFlow<List<RoutineGroup>> = repository.getAllRoutineGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createRoutine(name: String, description: String) {
        viewModelScope.launch {
            repository.saveRoutine(name, description, emptyList(), emptyMap())
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    fun createGroup(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.createRoutineGroup(trimmed)
        }
    }

    fun renameGroup(groupId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.renameRoutineGroup(groupId, trimmed)
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteRoutineGroup(groupId)
        }
    }

    fun shareRoutine(routine: Routine, context: Context) {
        viewModelScope.launch {
            val full = repository.getRoutineWithExercisesAndSets(routine.id) ?: return@launch
            val json = exportRoutineToJson(full)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_TEXT, json)
                putExtra(Intent.EXTRA_SUBJECT, "Strongest Routine: ${routine.name}")
            }
            val chooser = Intent.createChooser(shareIntent, "Share Routine")
            context.startActivity(chooser)
        }
    }

    fun importRoutine(jsonString: String) {
        viewModelScope.launch {
            try {
                val routine = importRoutineFromJson(jsonString) ?: return@launch
                repository.saveRoutine(routine.name, routine.description, routine.exercises, routine.sets)
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun exportRoutineToJson(full: RoutineWithExercisesAndSets): String {
        val exercisesById = repository.getAllExercisesList().associateBy { it.id }
        return full.toSharedRoutine(exercisesById).toJson()
    }

    private suspend fun importRoutineFromJson(jsonString: String): RoutineImportData? {
        val shared = jsonString.parseSharedRoutine() ?: return null

        val allExercises = repository.getAllExercisesList()
        val routineExercises = mutableListOf<RoutineExercise>()
        val routineSets = mutableMapOf<Long, List<RoutineSet>>()
        var tempId = -1L

        shared.exercises.forEachIndexed { index, sharedExercise ->
            // Find existing exercise by name, or create custom one
            var exercise = allExercises.find { it.name.equals(sharedExercise.name, ignoreCase = true) }
            if (exercise == null) {
                val customId = -(System.currentTimeMillis() + index)
                exercise = Exercise(
                    id = customId,
                    name = sharedExercise.name,
                    muscleGroup = sharedExercise.muscleGroup,
                    equipment = sharedExercise.equipment,
                    isCustom = true
                )
                repository.insertCustomExercise(exercise)
            }

            routineExercises.add(
                RoutineExercise(
                    id = tempId,
                    routineId = 0,
                    exerciseId = exercise.id,
                    orderIndex = index,
                    defaultSets = sharedExercise.defaultSets,
                    defaultWeight = sharedExercise.defaultWeight,
                    defaultReps = sharedExercise.defaultReps,
                    restSeconds = sharedExercise.restSeconds
                )
            )

            routineSets[tempId] = sharedExercise.sets.map { set ->
                RoutineSet(
                    id = 0,
                    routineExerciseId = tempId,
                    setNumber = set.setNumber,
                    weight = set.weight,
                    reps = set.reps,
                    restSeconds = set.restSeconds,
                    setType = set.setType
                )
            }
            tempId--
        }

        return RoutineImportData(shared.name, shared.description, routineExercises, routineSets)
    }

    private data class RoutineImportData(
        val name: String,
        val description: String,
        val exercises: List<RoutineExercise>,
        val sets: Map<Long, List<RoutineSet>>
    )
}
