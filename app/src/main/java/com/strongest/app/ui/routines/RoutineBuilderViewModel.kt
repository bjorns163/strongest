package com.strongest.app.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetType
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineExerciseUi(
    val exerciseId: Long,
    val exerciseName: String,
    val routineExerciseId: Long,
    val muscleGroup: String = "",
    val sets: List<RoutineSetUi>,
    val noteText: String = "",
    val previousSets: List<com.strongest.app.ui.workout.PreviousSetInfo> = emptyList()
)

data class RoutineSetUi(
    val setNumber: Int,
    val weight: Float = 0f,
    val reps: Int = 10,
    val restSeconds: Int = 90,
    val setType: SetType = SetType.NORMAL,
    val previousSetInfo: com.strongest.app.ui.workout.PreviousSetInfo? = null
)

data class RoutineBuilderState(
    val routineId: Long? = null,
    val routineName: String = "",
    val routineDescription: String = "",
    val groupId: Long? = null,
    val exercises: List<RoutineExerciseUi> = emptyList(),
    val isSaving: Boolean = false
)

@HiltViewModel
class RoutineBuilderViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoutineBuilderState())
    val state: StateFlow<RoutineBuilderState> = _state.asStateFlow()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    val groups: StateFlow<List<RoutineGroup>> = repository.getAllRoutineGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var defaultRestSeconds: Int = 90
    private var lastSetRestSeconds: Int = 150
    private var routineLoaded: Boolean = false
    private var tempIdCounter: Long = -1L
    private fun nextTempId(): Long = tempIdCounter--

    init {
        viewModelScope.launch {
            _exercises.value = repository.getAllExercises().first()
        }
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                defaultRestSeconds = settings.defaultRestSeconds
                lastSetRestSeconds = settings.lastSetRestSeconds
            }
        }
    }

    fun loadRoutine(routineId: Long) {
        if (routineLoaded) return
        routineLoaded = true
        viewModelScope.launch {
            val full = repository.getRoutineWithExercisesAndSets(routineId)
            if (full != null) {
                val uiExercises = full.exercises.map { re ->
                    val exercise = repository.getExerciseById(re.exerciseId)
                    val note = repository.getNote(re.exerciseId)
                    val previousSets = repository.getPreviousSessionSets(re.exerciseId)
                    val savedSets = full.sets[re.id] ?: emptyList()
                    val previousSetInfos = previousSets.map {
                        com.strongest.app.ui.workout.PreviousSetInfo(it.weightKg, it.reps)
                    }
                    val sets = if (savedSets.isNotEmpty()) {
                        savedSets.map { s ->
                            RoutineSetUi(
                                setNumber = s.setNumber,
                                weight = s.weight,
                                reps = s.reps,
                                restSeconds = s.restSeconds,
                                setType = s.setType,
                                previousSetInfo = previousSetInfos.getOrNull(s.setNumber - 1)
                            )
                        }
                    } else {
                        List(re.defaultSets) { setIdx ->
                            RoutineSetUi(
                                setNumber = setIdx + 1,
                                weight = re.defaultWeight,
                                reps = re.defaultReps,
                                restSeconds = re.restSeconds,
                                previousSetInfo = previousSetInfos.getOrNull(setIdx)
                            )
                        }
                    }
                    RoutineExerciseUi(
                        exerciseId = re.exerciseId,
                        exerciseName = exercise?.name ?: "Unknown",
                        routineExerciseId = re.id,
                        muscleGroup = exercise?.muscleGroup?.name ?: "",
                        noteText = note?.noteText ?: "",
                        sets = sets,
                        previousSets = previousSetInfos
                    )
                }
                _state.update {
                    it.copy(
                        routineId = routineId,
                        routineName = full.routine.name,
                        routineDescription = full.routine.description,
                        groupId = full.routine.groupId,
                        exercises = uiExercises
                    )
                }
            }
        }
    }

    fun addExercise(exerciseId: Long) {
        val allExercises = _exercises.value
        val found = allExercises.find { it.id == exerciseId } ?: return

        viewModelScope.launch {
            val previousSets = repository.getPreviousSessionSets(exerciseId)
            val defaultSetCount = if (previousSets.isNotEmpty()) previousSets.size else 3
            val defaultWeight = previousSets.firstOrNull()?.weightKg ?: 0f
            val defaultReps = previousSets.firstOrNull()?.reps ?: 10
            val note = repository.getNote(exerciseId)

            val previousSetInfos = previousSets.map {
                com.strongest.app.ui.workout.PreviousSetInfo(it.weightKg, it.reps)
            }
            val newExercise = RoutineExerciseUi(
                exerciseId = exerciseId,
                exerciseName = found.name,
                routineExerciseId = nextTempId(),
                muscleGroup = found.muscleGroup.name,
                noteText = note?.noteText ?: "",
                sets = List(defaultSetCount) { i ->
                    val prev = previousSets.getOrNull(i)
                    RoutineSetUi(
                        setNumber = i + 1,
                        weight = prev?.weightKg ?: defaultWeight,
                        reps = prev?.reps ?: defaultReps,
                        restSeconds = if (i == defaultSetCount - 1) lastSetRestSeconds else defaultRestSeconds,
                        previousSetInfo = previousSetInfos.getOrNull(i)
                    )
                },
                previousSets = previousSetInfos
            )
            _state.update { it.copy(exercises = it.exercises + newExercise) }
        }
    }

    fun addExercises(exerciseIds: List<Long>) {
        exerciseIds.forEach { addExercise(it) }
    }

    fun removeExercise(routineExerciseId: Long) {
        _state.update {
            it.copy(exercises = it.exercises.filter { e -> e.routineExerciseId != routineExerciseId })
        }
    }

    fun addSet(routineExerciseId: Long) {
        val exerciseIndex = _state.value.exercises.indexOfFirst { it.routineExerciseId == routineExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.exercises[exerciseIndex]
        val lastSet = exercise.sets.lastOrNull()
        val newSetNumber = (lastSet?.setNumber ?: 0) + 1
        val newSet = RoutineSetUi(
            setNumber = newSetNumber,
            weight = lastSet?.weight ?: 0f,
            reps = lastSet?.reps ?: 10,
            restSeconds = lastSetRestSeconds,
            setType = lastSet?.setType ?: SetType.NORMAL,
            previousSetInfo = exercise.previousSets.getOrNull(newSetNumber - 1)
        )

        val updatedExercises = _state.value.exercises.toMutableList()
        val updatedSets = exercise.sets.toMutableList()
        if (updatedSets.isNotEmpty()) {
            updatedSets[updatedSets.lastIndex] = updatedSets[updatedSets.lastIndex].copy(restSeconds = defaultRestSeconds)
        }
        updatedSets.add(newSet)
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(exercises = updatedExercises) }
    }

    fun deleteSet(routineExerciseId: Long, setIndex: Int) {
        val exerciseIndex = _state.value.exercises.indexOfFirst { it.routineExerciseId == routineExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.exercises[exerciseIndex]
        val wasLastSet = setIndex == exercise.sets.lastIndex
        val updatedSets = exercise.sets.toMutableList()
        updatedSets.removeAt(setIndex)

        val renumbered = updatedSets.mapIndexed { idx, s ->
            s.copy(
                setNumber = idx + 1,
                previousSetInfo = exercise.previousSets.getOrNull(idx)
            )
        }.toMutableList()
        if (wasLastSet && renumbered.isNotEmpty()) {
            renumbered[renumbered.lastIndex] = renumbered[renumbered.lastIndex].copy(restSeconds = lastSetRestSeconds)
        }

        val updatedExercises = _state.value.exercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = renumbered)
        _state.update { it.copy(exercises = updatedExercises) }
    }

    fun updateSet(routineExerciseId: Long, setIndex: Int, weight: Float, reps: Int) {
        val exerciseIndex = _state.value.exercises.indexOfFirst { it.routineExerciseId == routineExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.exercises[exerciseIndex]
        if (setIndex !in exercise.sets.indices) return
        val updatedSets = exercise.sets.toMutableList()
        updatedSets[setIndex] = updatedSets[setIndex].copy(weight = weight, reps = reps)
        for (i in (setIndex + 1)..updatedSets.lastIndex) {
            updatedSets[i] = updatedSets[i].copy(weight = weight, reps = reps)
        }

        val updatedExercises = _state.value.exercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(exercises = updatedExercises) }
    }

    fun updateSetRest(routineExerciseId: Long, setIndex: Int, restSeconds: Int) {
        val exerciseIndex = _state.value.exercises.indexOfFirst { it.routineExerciseId == routineExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.exercises[exerciseIndex]
        val updatedSets = exercise.sets.toMutableList()
        updatedSets[setIndex] = updatedSets[setIndex].copy(restSeconds = restSeconds)
        val lastIdx = updatedSets.lastIndex
        for (i in (setIndex + 1) until lastIdx) {
            updatedSets[i] = updatedSets[i].copy(restSeconds = restSeconds)
        }

        val updatedExercises = _state.value.exercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(exercises = updatedExercises) }
    }

    fun toggleWarmUp(routineExerciseId: Long, setIndex: Int) {
        val exerciseIndex = _state.value.exercises.indexOfFirst { it.routineExerciseId == routineExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.exercises[exerciseIndex]
        if (setIndex !in exercise.sets.indices) return
        val updatedSets = exercise.sets.toMutableList()
        val current = updatedSets[setIndex]
        updatedSets[setIndex] = current.copy(
            setType = if (current.setType == SetType.WARM_UP) SetType.NORMAL else SetType.WARM_UP
        )

        val updatedExercises = _state.value.exercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(exercises = updatedExercises) }
    }

    fun reorderExercise(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val exercises = _state.value.exercises.toMutableList()
        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)
        _state.update { it.copy(exercises = exercises) }
    }

    fun replaceExercise(routineExerciseId: Long, newExerciseId: Long) {
        viewModelScope.launch {
            val exerciseIndex = _state.value.exercises.indexOfFirst { it.routineExerciseId == routineExerciseId }
            if (exerciseIndex == -1) return@launch

            val oldExercise = _state.value.exercises[exerciseIndex]
            val allExercises = _exercises.value
            val newExercise = allExercises.find { it.id == newExerciseId } ?: return@launch

            val previousSets = repository.getPreviousSessionSets(newExerciseId)

            val previousSetInfos = previousSets.map {
                com.strongest.app.ui.workout.PreviousSetInfo(it.weightKg, it.reps)
            }
            val setCount = if (previousSets.isNotEmpty()) previousSets.size else oldExercise.sets.size.coerceAtLeast(1)
            val newSets = List(setCount) { i ->
                val prev = previousSets.getOrNull(i)
                RoutineSetUi(
                    setNumber = i + 1,
                    weight = prev?.weightKg ?: 0f,
                    reps = prev?.reps ?: 10,
                    restSeconds = if (i == setCount - 1) lastSetRestSeconds else defaultRestSeconds,
                    previousSetInfo = previousSetInfos.getOrNull(i)
                )
            }

            val updatedExercises = _state.value.exercises.toMutableList()
            updatedExercises[exerciseIndex] = oldExercise.copy(
                exerciseId = newExerciseId,
                exerciseName = newExercise.name,
                sets = newSets,
                previousSets = previousSetInfos
            )
            _state.update { it.copy(exercises = updatedExercises) }
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(routineName = name) }
    }

    fun updateDescription(desc: String) {
        _state.update { it.copy(routineDescription = desc) }
    }

    fun updateGroup(groupId: Long?) {
        _state.update { it.copy(groupId = groupId) }
    }

    fun saveRoutine() {
        if (_state.value.routineName.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val stateVal = _state.value
            val routineExercises = stateVal.exercises.mapIndexed { index, re ->
                val firstSet = re.sets.firstOrNull()
                RoutineExercise(
                    // Keep the original routineExerciseId (including negative temp values) so the
                    // repository can correctly look up per-set data in routineSetsMap.
                    // The repository inserts with id=0 anyway, so negative IDs are never written to DB.
                    id = re.routineExerciseId,
                    routineId = stateVal.routineId ?: 0,
                    exerciseId = re.exerciseId,
                    orderIndex = index,
                    defaultSets = re.sets.size,
                    defaultReps = firstSet?.reps ?: 10,
                    defaultWeight = firstSet?.weight ?: 0f,
                    restSeconds = firstSet?.restSeconds ?: defaultRestSeconds
                )
            }

            val routineSetsMap = mutableMapOf<Long, List<RoutineSet>>()
            for (re in stateVal.exercises) {
                val sets = re.sets.map { rs ->
                    RoutineSet(
                        routineExerciseId = rs.setNumber.toLong(), // temp placeholder
                        setNumber = rs.setNumber,
                        weight = rs.weight,
                        reps = rs.reps,
                        restSeconds = rs.restSeconds,
                        setType = rs.setType
                    )
                }
                routineSetsMap[re.routineExerciseId] = sets
            }

            if (stateVal.routineId != null) {
                val existing = repository.getRoutineWithExercises(stateVal.routineId).first
                if (existing != null) {
                    repository.updateRoutine(
                        existing.copy(
                            name = stateVal.routineName,
                            description = stateVal.routineDescription,
                            groupId = stateVal.groupId,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    repository.saveRoutineExercises(stateVal.routineId, routineExercises, routineSetsMap)
                }
            } else {
                repository.saveRoutine(
                    stateVal.routineName,
                    stateVal.routineDescription,
                    routineExercises,
                    routineSetsMap,
                    stateVal.groupId
                )
            }

            _state.update { it.copy(isSaving = false) }
        }
    }

    fun saveExerciseNote(exerciseId: Long, noteText: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.upsertNote(ExerciseNote(exerciseId = exerciseId, noteText = noteText, updatedAt = now))
            _state.update { state ->
                state.copy(
                    exercises = state.exercises.map { re ->
                        if (re.exerciseId == exerciseId) re.copy(noteText = noteText) else re
                    }
                )
            }
        }
    }
}
