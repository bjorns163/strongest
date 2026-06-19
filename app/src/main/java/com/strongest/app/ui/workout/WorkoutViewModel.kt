package com.strongest.app.ui.workout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseClassification
import com.strongest.app.data.model.ExerciseNote
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineExercise
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.RoutineSet
import com.strongest.app.data.model.SetLog
import com.strongest.app.data.model.SetType
import com.strongest.app.data.model.WorkoutExercise
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.utils.ACTION_COMPLETE_SET
import com.strongest.app.utils.ACTION_FINISH_WORKOUT
import com.strongest.app.utils.ACTION_SKIP_REST
import com.strongest.app.utils.ACTION_TIMER_ADD
import com.strongest.app.utils.ACTION_TIMER_SUBTRACT
import com.strongest.app.utils.WorkoutForegroundService
import com.strongest.app.utils.WorkoutNotificationBus
import com.strongest.app.utils.WorkoutNotificationState
import com.strongest.app.utils.WorkoutPrInfo
import com.strongest.app.utils.computeWorkoutPrs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val exercises: List<Exercise> = emptyList(),
    val routines: List<Routine> = emptyList(),
    val selectedMuscleGroup: MuscleGroup? = null,
    val selectedEquipment: Equipment? = null,
    val searchQuery: String = "",
    val ongoingWorkoutId: Long? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    val exercises = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val routines = repository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val routineGroups: StateFlow<List<RoutineGroup>> = repository.getAllRoutineGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val exerciseUsageCounts: StateFlow<Map<Long, Int>> = repository.getExerciseUsageCounts()
        .map { list -> list.associate { it.exerciseId to it.workoutCount } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedExercisesIfEmpty()
            val ongoing = repository.getOngoingWorkout()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    ongoingWorkoutId = ongoing?.id
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setMuscleGroup(group: MuscleGroup?) {
        _uiState.update { it.copy(selectedMuscleGroup = group) }
    }

    fun setEquipment(equipment: Equipment?) {
        _uiState.update { it.copy(selectedEquipment = equipment) }
    }

    fun startWorkout(routineId: Long? = null, routineName: String? = null) {
        viewModelScope.launch {
            val workoutId = repository.startWorkout(routineId, routineName)
            _uiState.update { it.copy(ongoingWorkoutId = workoutId) }
        }
    }

    fun resumeWorkout() {
        viewModelScope.launch {
            val ongoing = repository.getOngoingWorkout()
            _uiState.update { it.copy(ongoingWorkoutId = ongoing?.id) }
        }
    }

    fun cancelWorkout() {
        viewModelScope.launch {
            repository.getOngoingWorkout()?.let { ongoing ->
                repository.deleteWorkout(ongoing)
            }
            _uiState.update { it.copy(ongoingWorkoutId = null) }
        }
    }

    fun refreshOngoingWorkout() {
        viewModelScope.launch {
            val ongoing = repository.getOngoingWorkout()
            _uiState.update { it.copy(ongoingWorkoutId = ongoing?.id) }
        }
    }
}

data class ActiveWorkoutState(
    val workoutId: Long? = null,
    val workoutName: String? = null,
    val workoutExercises: List<WorkoutExerciseUi> = emptyList(),
    val isFinished: Boolean = false,
    val isViewMode: Boolean = false,
    val isEditingHistory: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val restTimerSeconds: Int = 90,
    val lastSetRestSeconds: Int = 150,
    val timerAdjustmentSeconds: Int = 30,
    val keepScreenOn: Boolean = false,
    val notificationSoundUri: String? = null,
    val rpeTrackingEnabled: Boolean = false,
    val activeTimerSetId: Long? = null,
    val timerRemainingSeconds: Int = 0,
    val timerTotalSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showFinishDialog: Boolean = false,
    val uncompletedSetsCount: Int = 0,
    val showExistingWorkoutDialog: Boolean = false,
    val showRoutineSaveDialog: Boolean = false,
    val showNewRoutineNameDialog: Boolean = false,
    val sourceRoutineId: Long? = null,
    val sourceRoutineExists: Boolean = false,
    val hasStructuralChanges: Boolean = false,
    val weightUnit: com.strongest.app.data.repository.WeightUnit = com.strongest.app.data.repository.WeightUnit.KG
)

data class WorkoutExerciseUi(
    val exerciseId: Long,
    val workoutExerciseId: Long,
    val exerciseName: String,
    val muscleGroup: MuscleGroup = MuscleGroup.OTHER,
    val equipment: Equipment = Equipment.NONE,
    val classification: ExerciseClassification = ExerciseClassification.ISOLATION,
    val sets: List<SetUi>,
    val previousSets: List<PreviousSetInfo> = emptyList(),
    val noteText: String = ""
)

data class SetUi(
    val setId: Long? = null,
    val setNumber: Int,
    val weight: Float = 0f,
    val reps: Int = 0,
    val rpe: Float? = null,
    val setType: SetType = SetType.NORMAL,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val restSeconds: Int = 90,
    val previousSetInfo: PreviousSetInfo? = null
)

data class PreviousSetInfo(
    val weight: Float,
    val reps: Int
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ActiveWorkoutState())
    val state: StateFlow<ActiveWorkoutState> = _state.asStateFlow()

    val workoutPrs: StateFlow<List<WorkoutPrInfo>> =
        combine(
            _state.map { it.workoutId }.distinctUntilChanged(),
            repository.getAllCompletedHistoryRows()
        ) { workoutId, rows ->
            if (workoutId == null) emptyList() else computeWorkoutPrs(rows, workoutId)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var timerJob: Job? = null
    private var workoutActionsReceiver: BroadcastReceiver? = null
    private var isWorkoutActionsReceiverRegistered = false
    private var isServiceStarted = false
    // True once the user has committed to finishing; stops the state collector from re-publishing
    // the ongoing-workout notification while a post-workout (e.g. routine-save) dialog is shown.
    private var isFinishing = false
    private var timerEndTime: Long = 0
    private val persistOrderMutex = Mutex()

    private fun ensureWorkoutActionsReceiverRegistered() {
        if (isWorkoutActionsReceiverRegistered) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val adjustment = _state.value.timerAdjustmentSeconds
                when (intent?.action) {
                    ACTION_TIMER_ADD -> adjustTimer(adjustment)
                    ACTION_TIMER_SUBTRACT -> adjustTimer(-adjustment)
                    ACTION_COMPLETE_SET -> completeCurrentSetFromNotification()
                    ACTION_SKIP_REST -> skipRestTimer()
                    ACTION_FINISH_WORKOUT -> doFinishWorkout()
                }
            }
        }
        workoutActionsReceiver = receiver
        val filter = IntentFilter().apply {
            addAction(ACTION_TIMER_ADD)
            addAction(ACTION_TIMER_SUBTRACT)
            addAction(ACTION_COMPLETE_SET)
            addAction(ACTION_SKIP_REST)
            addAction(ACTION_FINISH_WORKOUT)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        isWorkoutActionsReceiverRegistered = true
    }

    private fun unregisterWorkoutActionsReceiver() {
        if (!isWorkoutActionsReceiverRegistered) return
        workoutActionsReceiver?.let {
            try { context.unregisterReceiver(it) } catch (_: Exception) {}
        }
        workoutActionsReceiver = null
        isWorkoutActionsReceiverRegistered = false
    }

    private fun ensureServiceStarted() {
        // A fresh/resumed workout is not in the finishing state.
        isFinishing = false
        if (isServiceStarted) return
        WorkoutForegroundService.start(context)
        isServiceStarted = true
    }

    private fun stopServiceAndClearNotification() {
        WorkoutNotificationBus.clear()
        if (isServiceStarted) {
            WorkoutForegroundService.stop(context)
            isServiceStarted = false
        }
    }

    private suspend fun persistExerciseOrderAndIds() = persistOrderMutex.withLock {
        val workoutId = _state.value.workoutId ?: return@withLock
        // Snapshot the list inside the lock so we don't race with concurrent state mutations.
        val snapshot = _state.value.workoutExercises
        snapshot.forEachIndexed { idx, ex ->
            repository.updateWorkoutExercise(
                WorkoutExercise(
                    id = ex.workoutExerciseId,
                    workoutId = workoutId,
                    exerciseId = ex.exerciseId,
                    orderIndex = idx
                )
            )
        }
    }

    private fun findNextUncompletedSet(): Pair<WorkoutExerciseUi, SetUi>? {
        for (ex in _state.value.workoutExercises) {
            for (set in ex.sets) {
                if (!set.isCompleted) return ex to set
            }
        }
        return null
    }

    private fun completeCurrentSetFromNotification() {
        val next = findNextUncompletedSet() ?: return
        val (ex, set) = next
        val setIndex = ex.sets.indexOf(set)
        if (setIndex >= 0) {
            logSet(ex.workoutExerciseId, setIndex)
        }
    }

    private fun publishNotificationState() {
        val s = _state.value
        if (s.workoutId == null || s.isViewMode || s.isFinished || isFinishing) return
        val name = s.workoutName?.takeIf { it.isNotBlank() } ?: "Workout"

        if (s.isTimerRunning && s.timerRemainingSeconds > 0) {
            val next = findNextUncompletedSet()
            val notif = if (next != null) {
                val (ex, set) = next
                WorkoutNotificationState.Resting(
                    workoutName = name,
                    workoutStartTime = s.startTime,
                    nextExerciseName = ex.exerciseName,
                    nextSetNumber = set.setNumber,
                    nextWeightKg = set.weight,
                    nextReps = set.reps,
                    remainingSeconds = s.timerRemainingSeconds,
                    totalSeconds = s.timerTotalSeconds,
                    adjustmentSeconds = s.timerAdjustmentSeconds,
                    weightUnit = s.weightUnit
                )
            } else {
                WorkoutNotificationState.AllDone(name, s.startTime, s.weightUnit)
            }
            WorkoutNotificationBus.publish(notif)
            return
        }

        val next = findNextUncompletedSet()
        val notif = when {
            next != null -> {
                val (ex, set) = next
                WorkoutNotificationState.SetReady(
                    workoutName = name,
                    workoutStartTime = s.startTime,
                    exerciseName = ex.exerciseName,
                    setNumber = set.setNumber,
                    totalSetsInExercise = ex.sets.size,
                    weightKg = set.weight,
                    reps = set.reps,
                    weightUnit = s.weightUnit
                )
            }
            s.workoutExercises.isEmpty() -> WorkoutNotificationState.WaitingForExercises(name, s.startTime, s.weightUnit)
            else -> WorkoutNotificationState.AllDone(name, s.startTime, s.weightUnit)
        }
        WorkoutNotificationBus.publish(notif)
    }

    fun adjustTimer(seconds: Int) {
        viewModelScope.launch {
            timerEndTime += (seconds * 1000L)
            val remaining = ((timerEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
            _state.update {
                it.copy(
                    timerRemainingSeconds = remaining,
                    timerTotalSeconds = maxOf(it.timerTotalSeconds, remaining)
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _state.update {
                    it.copy(
                        restTimerSeconds = settings.defaultRestSeconds,
                        lastSetRestSeconds = settings.lastSetRestSeconds,
                        timerAdjustmentSeconds = settings.timerAdjustmentSeconds,
                        keepScreenOn = settings.keepScreenOn,
                        notificationSoundUri = settings.notificationSoundUri,
                        rpeTrackingEnabled = settings.rpeTrackingEnabled,
                        weightUnit = settings.weightUnit
                    )
                }
            }
        }
        viewModelScope.launch {
            _state.collect { publishNotificationState() }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        unregisterWorkoutActionsReceiver()
        super.onCleared()
    }

    fun loadWorkout(workoutId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(workoutId = workoutId) }
            val workout = repository.getWorkoutById(workoutId)
            if (workout != null) {
                _state.update {
                    it.copy(
                        workoutName = workout.workoutName ?: workout.routineName,
                        startTime = workout.startTime,
                        sourceRoutineId = workout.routineId
                    )
                }
            }
            loadWorkoutExercises(workoutId, isOngoing = true)
            ensureWorkoutActionsReceiverRegistered()
            ensureServiceStarted()
        }
    }

    private var isInitializingWorkout = false
    private var pendingRoutineId: Long? = null

    fun startNewWorkoutIfNeeded() {
        if (_state.value.workoutId != null || isInitializingWorkout) return
        isInitializingWorkout = true
        viewModelScope.launch {
            val existingOngoing = repository.getOngoingWorkout()
            if (existingOngoing != null) {
                pendingRoutineId = null
                _state.update { it.copy(showExistingWorkoutDialog = true) }
                isInitializingWorkout = false
                return@launch
            }

            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val defaultName = when (hour) {
                in 5..11 -> "Morning Workout"
                in 12..16 -> "Afternoon Workout"
                else -> "Evening Workout"
            }
            val startTime = System.currentTimeMillis()
            val workoutId = repository.startWorkout(null, null, defaultName)
            _state.update { it.copy(workoutId = workoutId, workoutName = defaultName, startTime = startTime) }
            ensureWorkoutActionsReceiverRegistered()
            ensureServiceStarted()
            isInitializingWorkout = false
        }
    }

    fun resumeExistingWorkout() {
        viewModelScope.launch {
            val existing = repository.getOngoingWorkout() ?: return@launch
            _state.update { it.copy(showExistingWorkoutDialog = false) }
            loadWorkout(existing.id)
        }
    }

    fun discardExistingAndStartNew() {
        viewModelScope.launch {
            val existing = repository.getOngoingWorkout()
            if (existing != null) {
                repository.deleteWorkout(existing)
            }
            _state.update { it.copy(showExistingWorkoutDialog = false) }
            val routineId = pendingRoutineId
            pendingRoutineId = null
            if (routineId != null) {
                startWorkoutFromRoutine(routineId)
            } else {
                startNewWorkoutIfNeeded()
            }
        }
    }

    fun startWorkoutFromRoutine(routineId: Long) {
        if (_state.value.workoutId != null || isInitializingWorkout) return
        isInitializingWorkout = true
        viewModelScope.launch {
            val existingOngoing = repository.getOngoingWorkout()
            if (existingOngoing != null) {
                pendingRoutineId = routineId
                _state.update { it.copy(showExistingWorkoutDialog = true) }
                isInitializingWorkout = false
                return@launch
            }
            val full = repository.getRoutineWithExercisesAndSets(routineId)
            val routine = full?.routine
            val routineExercises = full?.exercises ?: emptyList()
            val routineSets = full?.sets ?: emptyMap()
            val routineName = routine?.name ?: "Workout"
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val workoutName = when (hour) {
                in 5..11 -> "Morning $routineName"
                in 12..16 -> "Afternoon $routineName"
                else -> "Evening $routineName"
            }
            val startTime = System.currentTimeMillis()
            val workoutId = repository.startWorkout(routineId, routineName, workoutName)

            val uiExercises = mutableListOf<WorkoutExerciseUi>()
            for (re in routineExercises) {
                val exercise = repository.getExerciseById(re.exerciseId)
                val previousSets = repository.getPreviousSessionSets(re.exerciseId)
                    .map { PreviousSetInfo(it.weightKg, it.reps) }
                val workoutExerciseId = repository.addExerciseToWorkout(workoutId, re.exerciseId, uiExercises.size)
                val note = repository.getNote(re.exerciseId)

                val savedSets = routineSets[re.id] ?: emptyList()
                val setCount = if (savedSets.isNotEmpty()) savedSets.size else re.defaultSets

                val uiSets = List(setCount) { setIdx ->
                    val saved = savedSets.getOrNull(setIdx)
                    val weight = saved?.weight ?: re.defaultWeight
                    val reps = saved?.reps ?: re.defaultReps
                    val restSec = if (setIdx == setCount - 1) {
                        _state.value.lastSetRestSeconds
                    } else {
                        saved?.restSeconds ?: re.restSeconds
                    }

                    val setId = repository.logSet(
                        workoutExerciseId, setIdx + 1,
                        weight, reps, null, SetType.NORMAL,
                        restSeconds = restSec, completedAt = 0
                    )
                    SetUi(
                        setId = setId,
                        setNumber = setIdx + 1,
                        weight = weight,
                        reps = reps,
                        setType = SetType.NORMAL,
                        restSeconds = restSec,
                        previousSetInfo = previousSets.getOrNull(setIdx)
                    )
                }
                uiExercises.add(
                    WorkoutExerciseUi(
                        exerciseId = re.exerciseId,
                        workoutExerciseId = workoutExerciseId,
                        exerciseName = exercise?.name ?: "Unknown",
                        muscleGroup = exercise?.muscleGroup ?: MuscleGroup.OTHER,
                        equipment = exercise?.equipment ?: com.strongest.app.data.model.Equipment.NONE,
                        classification = exercise?.classification ?: com.strongest.app.data.model.ExerciseClassification.ISOLATION,
                        noteText = note?.noteText ?: "",
                        sets = uiSets,
                        previousSets = previousSets
                    )
                )
            }

            _state.update {
                it.copy(
                    workoutId = workoutId,
                    workoutName = workoutName,
                    workoutExercises = uiExercises,
                    startTime = startTime,
                    sourceRoutineId = routineId
                )
            }
            ensureWorkoutActionsReceiverRegistered()
            ensureServiceStarted()
            isInitializingWorkout = false
        }
    }

    fun updateWorkoutName(name: String) {
        _state.update { it.copy(workoutName = name) }
    }

    private suspend fun loadWorkoutExercises(workoutId: Long, isOngoing: Boolean = false) {
        val details = repository.getWorkoutWithDetails(workoutId)
        if (details != null) {
            val exercisesUi = details.exercises.map { exerciseWithSets ->
                val exercise = repository.getExerciseById(exerciseWithSets.workoutExercise.exerciseId)
                val previousSets = repository.getPreviousSessionSets(
                    exerciseWithSets.workoutExercise.exerciseId
                ).map { PreviousSetInfo(it.weightKg, it.reps) }

                val note = repository.getNote(exerciseWithSets.workoutExercise.exerciseId)

                val setsToShow = if (isOngoing) {
                    exerciseWithSets.sets
                } else {
                    exerciseWithSets.sets.filter { it.completedAt > 0 }
                }

                WorkoutExerciseUi(
                    exerciseId = exerciseWithSets.workoutExercise.exerciseId,
                    workoutExerciseId = exerciseWithSets.workoutExercise.id,
                    exerciseName = exercise?.name ?: "Unknown",
                    muscleGroup = exercise?.muscleGroup ?: MuscleGroup.OTHER,
                    equipment = exercise?.equipment ?: com.strongest.app.data.model.Equipment.NONE,
                    classification = exercise?.classification ?: com.strongest.app.data.model.ExerciseClassification.ISOLATION,
                    noteText = note?.noteText ?: "",
                    sets = setsToShow.map { set ->
                        val prevInfo = previousSets.getOrNull(set.setNumber - 1)
                        SetUi(
                            setId = set.id,
                            setNumber = set.setNumber,
                            weight = set.weightKg,
                            reps = set.reps,
                            rpe = set.rpe,
                            setType = set.setType,
                            isCompleted = set.completedAt > 0,
                            completedAt = set.completedAt,
                            restSeconds = set.restSeconds,
                            previousSetInfo = prevInfo
                        )
                    },
                    previousSets = previousSets
                )
            }
            _state.update { it.copy(workoutExercises = exercisesUi) }
        }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            val orderIndex = _state.value.workoutExercises.size
            val workoutExerciseId = repository.addExerciseToWorkout(workoutId, exerciseId, orderIndex)
            val exercise = repository.getExerciseById(exerciseId)
            val previousSets = repository.getPreviousSessionSets(exerciseId)
                .map { PreviousSetInfo(it.weightKg, it.reps) }
            val defaultRest = _state.value.restTimerSeconds
            val lastSetRest = _state.value.lastSetRestSeconds
            val note = repository.getNote(exerciseId)

            val setCount = if (previousSets.isNotEmpty()) previousSets.size else 1
            val uiSets = mutableListOf<SetUi>()
            for (setIdx in 0 until setCount) {
                val rest = if (setIdx == setCount - 1) lastSetRest else defaultRest
                val setId = repository.logSet(
                    workoutExerciseId, setIdx + 1,
                    previousSets.getOrNull(setIdx)?.weight ?: 0f,
                    previousSets.getOrNull(setIdx)?.reps ?: 0,
                    null, SetType.NORMAL, restSeconds = rest, completedAt = 0
                )
                uiSets.add(
                    SetUi(
                        setId = setId,
                        setNumber = setIdx + 1,
                        weight = previousSets.getOrNull(setIdx)?.weight ?: 0f,
                        reps = previousSets.getOrNull(setIdx)?.reps ?: 0,
                        setType = SetType.NORMAL,
                        restSeconds = rest,
                        previousSetInfo = previousSets.getOrNull(setIdx)
                    )
                )
            }

            val newExercise = WorkoutExerciseUi(
                exerciseId = exerciseId,
                workoutExerciseId = workoutExerciseId,
                exerciseName = exercise?.name ?: "Unknown",
                muscleGroup = exercise?.muscleGroup ?: MuscleGroup.OTHER,
                equipment = exercise?.equipment ?: com.strongest.app.data.model.Equipment.NONE,
                classification = exercise?.classification ?: com.strongest.app.data.model.ExerciseClassification.ISOLATION,
                noteText = note?.noteText ?: "",
                sets = uiSets,
                previousSets = previousSets
            )
            _state.update {
                it.copy(
                    workoutExercises = it.workoutExercises + newExercise
                )
            }
            persistExerciseOrderAndIds()
        }
    }

    fun addExercises(exerciseIds: List<Long>) {
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            val defaultRest = _state.value.restTimerSeconds
            val lastSetRest = _state.value.lastSetRestSeconds
            val newExercises = mutableListOf<WorkoutExerciseUi>()
            for (exerciseId in exerciseIds) {
                val orderIndex = _state.value.workoutExercises.size + newExercises.size
                val workoutExerciseId = repository.addExerciseToWorkout(workoutId, exerciseId, orderIndex)
                val exercise = repository.getExerciseById(exerciseId)
                val previousSets = repository.getPreviousSessionSets(exerciseId)
                    .map { PreviousSetInfo(it.weightKg, it.reps) }
                val note = repository.getNote(exerciseId)

                val setCount = if (previousSets.isNotEmpty()) previousSets.size else 1
                val uiSets = mutableListOf<SetUi>()
                for (setIdx in 0 until setCount) {
                    val rest = if (setIdx == setCount - 1) lastSetRest else defaultRest
                    val setId = repository.logSet(
                        workoutExerciseId, setIdx + 1,
                        previousSets.getOrNull(setIdx)?.weight ?: 0f,
                        previousSets.getOrNull(setIdx)?.reps ?: 0,
                        null, SetType.NORMAL, restSeconds = rest, completedAt = 0
                    )
                    uiSets.add(
                        SetUi(
                            setId = setId,
                            setNumber = setIdx + 1,
                            weight = previousSets.getOrNull(setIdx)?.weight ?: 0f,
                            reps = previousSets.getOrNull(setIdx)?.reps ?: 0,
                            setType = SetType.NORMAL,
                            restSeconds = rest,
                            previousSetInfo = previousSets.getOrNull(setIdx)
                        )
                    )
                }

                newExercises.add(
                    WorkoutExerciseUi(
                        exerciseId = exerciseId,
                        workoutExerciseId = workoutExerciseId,
                        exerciseName = exercise?.name ?: "Unknown",
                        muscleGroup = exercise?.muscleGroup ?: MuscleGroup.OTHER,
                        equipment = exercise?.equipment ?: com.strongest.app.data.model.Equipment.NONE,
                        classification = exercise?.classification ?: com.strongest.app.data.model.ExerciseClassification.ISOLATION,
                        noteText = note?.noteText ?: "",
                        sets = uiSets,
                        previousSets = previousSets
                    )
                )
            }
            _state.update {
                it.copy(
                    workoutExercises = it.workoutExercises + newExercises
                )
            }
            persistExerciseOrderAndIds()
        }
    }

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            repository.removeExerciseFromWorkout(workoutExerciseId)
            _state.update {
                it.copy(
                    workoutExercises = it.workoutExercises.filter { e -> e.workoutExerciseId != workoutExerciseId }
                )
            }
            persistExerciseOrderAndIds()
        }
    }

    fun reorderExercise(fromIndex: Int, toIndex: Int) {
        val exercises = _state.value.workoutExercises.toMutableList()
        if (fromIndex !in exercises.indices || toIndex !in exercises.indices) return
        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)
        _state.update { it.copy(workoutExercises = exercises) }
        viewModelScope.launch { persistExerciseOrderAndIds() }
    }

    fun addSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            val exerciseIndex = _state.value.workoutExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
            if (exerciseIndex == -1) return@launch

            val exercise = _state.value.workoutExercises[exerciseIndex]
            val lastSet = exercise.sets.lastOrNull()
            val newSetNumber = (lastSet?.setNumber ?: 0) + 1
            val defaultRest = _state.value.restTimerSeconds
            val lastSetRest = _state.value.lastSetRestSeconds

            val prevInfo = exercise.previousSets.getOrNull(newSetNumber - 1)

            val setId = repository.logSet(
                workoutExerciseId, newSetNumber,
                lastSet?.weight ?: 0f, lastSet?.reps ?: 0, null, lastSet?.setType ?: SetType.NORMAL,
                restSeconds = lastSetRest, completedAt = 0
            )

            val updatedSets = exercise.sets.toMutableList()
            if (updatedSets.isNotEmpty()) {
                val idx = updatedSets.lastIndex
                val demoted = updatedSets[idx].copy(restSeconds = defaultRest)
                updatedSets[idx] = demoted
                persistSet(workoutExerciseId, demoted)
            }

            val newSet = SetUi(
                setId = setId,
                setNumber = newSetNumber,
                weight = lastSet?.weight ?: 0f,
                reps = lastSet?.reps ?: 0,
                setType = lastSet?.setType ?: SetType.NORMAL,
                restSeconds = lastSetRest,
                previousSetInfo = prevInfo
            )

            updatedSets.add(newSet)
            val updatedExercises = _state.value.workoutExercises.toMutableList()
            updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
            _state.update { it.copy(workoutExercises = updatedExercises) }
        }
    }

    private fun persistSet(workoutExerciseId: Long, set: SetUi) {
        val id = set.setId ?: return
        viewModelScope.launch {
            repository.updateSet(
                com.strongest.app.data.model.SetLog(
                    id = id,
                    workoutExerciseId = workoutExerciseId,
                    setNumber = set.setNumber,
                    weightKg = set.weight,
                    reps = set.reps,
                    rpe = set.rpe,
                    setType = set.setType,
                    restSeconds = set.restSeconds,
                    completedAt = set.completedAt
                )
            )
        }
    }

    fun updateSet(workoutExerciseId: Long, setIndex: Int, weight: Float, reps: Int) {
        val exerciseIndex = _state.value.workoutExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.workoutExercises[exerciseIndex]
        if (setIndex !in exercise.sets.indices) return

        val updatedSets = exercise.sets.toMutableList()
        updatedSets[setIndex] = updatedSets[setIndex].copy(weight = weight, reps = reps)

        // Fill down weight/reps to all uncompleted sets below this one
        for (i in (setIndex + 1)..updatedSets.lastIndex) {
            if (!updatedSets[i].isCompleted) {
                updatedSets[i] = updatedSets[i].copy(weight = weight, reps = reps)
            }
        }

        val updatedExercises = _state.value.workoutExercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(workoutExercises = updatedExercises) }

        for (i in setIndex..updatedSets.lastIndex) {
            if (i == setIndex || !exercise.sets[i].isCompleted) {
                persistSet(workoutExerciseId, updatedSets[i])
            }
        }
    }

    fun updateSetRest(workoutExerciseId: Long, setIndex: Int, restSeconds: Int) {
        val exerciseIndex = _state.value.workoutExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.workoutExercises[exerciseIndex]
        if (setIndex !in exercise.sets.indices) return

        val updatedSets = exercise.sets.toMutableList()
        updatedSets[setIndex] = updatedSets[setIndex].copy(restSeconds = restSeconds)

        // Fill down rest to uncompleted sets below, but never overwrite the last set's timer
        val lastIdx = updatedSets.lastIndex
        for (i in (setIndex + 1) until lastIdx) {
            if (!updatedSets[i].isCompleted) {
                updatedSets[i] = updatedSets[i].copy(restSeconds = restSeconds)
            }
        }

        val updatedExercises = _state.value.workoutExercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(workoutExercises = updatedExercises) }

        persistSet(workoutExerciseId, updatedSets[setIndex])
        for (i in (setIndex + 1) until lastIdx) {
            if (!exercise.sets[i].isCompleted) {
                persistSet(workoutExerciseId, updatedSets[i])
            }
        }
    }

    fun updateSetRpe(workoutExerciseId: Long, setIndex: Int, rpe: Float?) {
        val exerciseIndex = _state.value.workoutExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
        if (exerciseIndex == -1) return

        val exercise = _state.value.workoutExercises[exerciseIndex]
        if (setIndex !in exercise.sets.indices) return
        val updatedSet = exercise.sets[setIndex].copy(rpe = rpe)
        val updatedSets = exercise.sets.toMutableList().apply { this[setIndex] = updatedSet }

        val updatedExercises = _state.value.workoutExercises.toMutableList()
        updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.update { it.copy(workoutExercises = updatedExercises) }

        persistSet(workoutExerciseId, updatedSet)
    }

    fun logSet(workoutExerciseId: Long, setIndex: Int) {
        viewModelScope.launch {
            val exercise = _state.value.workoutExercises.find { it.workoutExerciseId == workoutExerciseId } ?: return@launch
            val set = exercise.sets[setIndex]
            // Preserve the existing completedAt if this set was already completed; only stamp now on first completion.
            val completedAt = if (set.isCompleted && set.completedAt > 0) set.completedAt else System.currentTimeMillis()

            val resolvedSetId = if (set.setId != null) {
                repository.updateSet(
                    com.strongest.app.data.model.SetLog(
                        id = set.setId,
                        workoutExerciseId = workoutExerciseId,
                        setNumber = set.setNumber,
                        weightKg = set.weight,
                        reps = set.reps,
                        rpe = set.rpe,
                        setType = set.setType,
                        restSeconds = set.restSeconds,
                        completedAt = completedAt
                    )
                )
                set.setId
            } else {
                repository.logSet(
                    workoutExerciseId, set.setNumber, set.weight, set.reps, set.rpe, set.setType,
                    restSeconds = set.restSeconds, completedAt = completedAt
                )
            }

            val updatedSets = exercise.sets.toMutableList()
            updatedSets[setIndex] = set.copy(
                setId = resolvedSetId,
                isCompleted = true,
                completedAt = completedAt
            )
            val updatedExercises = _state.value.workoutExercises.toMutableList()
            val exerciseIndex = updatedExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
            updatedExercises[exerciseIndex] = exercise.copy(sets = updatedSets)
            _state.update { it.copy(workoutExercises = updatedExercises) }
            startRestTimer(resolvedSetId, set.restSeconds)
        }
    }

    fun deleteSet(workoutExerciseId: Long, setIndex: Int) {
        viewModelScope.launch {
            val exercise = _state.value.workoutExercises.find { it.workoutExerciseId == workoutExerciseId } ?: return@launch
            val set = exercise.sets[setIndex]

            if (set.setId != null) {
                repository.deleteSet(
                    com.strongest.app.data.model.SetLog(
                        id = set.setId,
                        workoutExerciseId = workoutExerciseId,
                        setNumber = set.setNumber,
                        weightKg = set.weight,
                        reps = set.reps,
                        setType = set.setType
                    )
                )
            }

            val wasLastSet = setIndex == exercise.sets.lastIndex
            val updatedSets = exercise.sets.toMutableList()
            updatedSets.removeAt(setIndex)

            val renumberedSets = updatedSets.mapIndexed { idx, s ->
                s.copy(
                    setNumber = idx + 1,
                    previousSetInfo = exercise.previousSets.getOrNull(idx)
                )
            }.toMutableList()
            if (wasLastSet && renumberedSets.isNotEmpty()) {
                val lastIdx = renumberedSets.lastIndex
                renumberedSets[lastIdx] = renumberedSets[lastIdx].copy(restSeconds = _state.value.lastSetRestSeconds)
            }

            val updatedExercises = _state.value.workoutExercises.toMutableList()
            val exerciseIndex = updatedExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
            updatedExercises[exerciseIndex] = exercise.copy(sets = renumberedSets)
            _state.update { it.copy(workoutExercises = updatedExercises) }
        }
    }

    fun startRestTimer(setId: Long?, durationSeconds: Int) {
        timerJob?.cancel()
        ensureWorkoutActionsReceiverRegistered()
        timerEndTime = System.currentTimeMillis() + (durationSeconds * 1000L)
        _state.update {
            it.copy(
                activeTimerSetId = setId,
                timerRemainingSeconds = durationSeconds,
                timerTotalSeconds = durationSeconds,
                isTimerRunning = true
            )
        }

        timerJob = viewModelScope.launch {
            while (isActive) {
                val remaining = ((timerEndTime - System.currentTimeMillis()) / 1000).toInt()
                if (remaining <= 0) {
                    _state.update { it.copy(isTimerRunning = false, timerRemainingSeconds = 0, activeTimerSetId = null) }
                    playTimerAlert()
                    break
                }
                delay(1000)
                _state.update { it.copy(timerRemainingSeconds = remaining) }
            }
        }
    }

    fun cancelRestTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(isTimerRunning = false, timerRemainingSeconds = 0, activeTimerSetId = null) }
    }

    fun skipRestTimer() {
        cancelRestTimer()
    }

    private fun playTimerAlert() {
        try {
            val alertUri = _state.value.notificationSoundUri?.let { Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alertUri)
            ringtone?.play()
        } catch (_: Exception) {
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 200), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE)
                    as android.os.Vibrator
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200, 100, 200), -1))
            }
        } catch (_: Exception) {
        }
    }

    fun finishWorkout() {
        val uncompletedCount = _state.value.workoutExercises
            .flatMap { it.sets }
            .count { !it.isCompleted }

        if (uncompletedCount > 0) {
            _state.update { it.copy(showFinishDialog = true, uncompletedSetsCount = uncompletedCount) }
        } else {
            proceedToRoutineSaveOrFinish()
        }
    }

    fun markUncompletedAsDone() {
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            // Mark finishing up front: marking every set complete below produces an "all done" state
            // that the notification collector would otherwise publish before we get to clear it.
            isFinishing = true
            val now = System.currentTimeMillis()

            val updatedExercises = _state.value.workoutExercises.map { exercise ->
                val updatedSets = exercise.sets.map { set ->
                    if (!set.isCompleted && set.setId != null) {
                        repository.updateSet(
                            SetLog(
                                id = set.setId,
                                workoutExerciseId = exercise.workoutExerciseId,
                                setNumber = set.setNumber,
                                weightKg = set.weight,
                                reps = set.reps,
                                rpe = set.rpe,
                                setType = set.setType,
                                restSeconds = set.restSeconds,
                                completedAt = now
                            )
                        )
                        set.copy(isCompleted = true, completedAt = now)
                    } else set
                }
                exercise.copy(sets = updatedSets)
            }
            _state.update { it.copy(workoutExercises = updatedExercises) }

            proceedToRoutineSaveOrFinish()
        }
    }

    fun discardUncompletedSets() {
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            isFinishing = true

            for (exercise in _state.value.workoutExercises) {
                for (set in exercise.sets) {
                    if (!set.isCompleted && set.setId != null) {
                        repository.deleteSet(
                            SetLog(
                                id = set.setId,
                                workoutExerciseId = exercise.workoutExerciseId,
                                setNumber = set.setNumber,
                                weightKg = set.weight,
                                reps = set.reps,
                                setType = set.setType
                            )
                        )
                    }
                }
            }

            // Drop any exercises that ended up with no completed sets so they don't linger
            // in history or the saved workout detail.
            repository.removeEmptyWorkoutExercises(workoutId)
            _state.update { state ->
                state.copy(
                    workoutExercises = state.workoutExercises.filter { ex ->
                        ex.sets.any { it.isCompleted }
                    }
                )
            }

            proceedToRoutineSaveOrFinish()
        }
    }

    fun dismissFinishDialog() {
        _state.update { it.copy(showFinishDialog = false, uncompletedSetsCount = 0) }
    }

    private fun proceedToRoutineSaveOrFinish() {
        // The user has committed to finishing, so the active session is over: stop the rest timer
        // and clear the ongoing-workout notification now. This is reached from every finish path
        // (discard, mark-done, empty, clean finish); doing it here ensures the notification clears
        // even when a routine-save dialog is shown next. doFinishWorkout() repeats this idempotently.
        // isFinishing keeps the _state collector from re-publishing the notification while that
        // dialog is open (a non-routine/empty workout doesn't set isFinished until doFinishWorkout).
        isFinishing = true
        cancelRestTimer()
        stopServiceAndClearNotification()
        viewModelScope.launch {
            val sourceRoutineId = _state.value.sourceRoutineId
            val workoutExercises = _state.value.workoutExercises
            if (workoutExercises.isEmpty()) {
                doFinishWorkout()
                return@launch
            }

            val routineFull = sourceRoutineId?.let { repository.getRoutineWithExercisesAndSets(it) }
            val sourceRoutineExists = routineFull != null

            var hasStructuralChanges = false
            var hasValueChanges = false
            if (routineFull != null) {
                val routineExerciseIds = routineFull.exercises.map { it.exerciseId }
                val workoutExerciseIds = workoutExercises.map { it.exerciseId }
                hasStructuralChanges = routineExerciseIds != workoutExerciseIds
                if (!hasStructuralChanges) {
                    outer@ for ((idx, we) in workoutExercises.withIndex()) {
                        val re = routineFull.exercises[idx]
                        val routineSets = routineFull.sets[re.id] ?: emptyList()
                        if (routineSets.size != we.sets.size) {
                            hasValueChanges = true
                            break
                        }
                        for ((sIdx, ws) in we.sets.withIndex()) {
                            val rs = routineSets[sIdx]
                            if (rs.weight != ws.weight || rs.reps != ws.reps) {
                                hasValueChanges = true
                                break@outer
                            }
                        }
                    }
                }
            }

            val noChangesFromRoutine = sourceRoutineExists && !hasStructuralChanges && !hasValueChanges
            if (noChangesFromRoutine) {
                doFinishWorkout()
                return@launch
            }

            _state.update {
                it.copy(
                    showFinishDialog = false,
                    uncompletedSetsCount = 0,
                    showRoutineSaveDialog = true,
                    sourceRoutineExists = sourceRoutineExists,
                    hasStructuralChanges = hasStructuralChanges
                )
            }
        }
    }

    fun dismissRoutineSaveDialog() {
        _state.update { it.copy(showRoutineSaveDialog = false) }
        doFinishWorkout()
    }

    fun updateRoutineSetsOnlyAndFinish() {
        viewModelScope.launch {
            val routineId = _state.value.sourceRoutineId
            if (routineId != null) {
                // Position-aligned with the routine's exercise list (only reached when there are
                // no structural changes), so each workout exercise maps 1:1 to a routine exercise.
                val setsByPosition = _state.value.workoutExercises.map { ex ->
                    ex.sets.mapIndexed { idx, s ->
                        RoutineSet(
                            routineExerciseId = 0,
                            setNumber = idx + 1,
                            weight = s.weight,
                            reps = s.reps,
                            restSeconds = s.restSeconds
                        )
                    }
                }
                repository.updateRoutineSetsOnly(routineId, setsByPosition)
            }
            _state.update { it.copy(showRoutineSaveDialog = false) }
            doFinishWorkout()
        }
    }

    fun updateRoutineFullAndFinish() {
        viewModelScope.launch {
            val routineId = _state.value.sourceRoutineId
            val routine = routineId?.let { repository.getRoutineById(it) }
            if (routineId != null && routine != null) {
                val (exercises, setsMap) = buildRoutineFromWorkout()
                repository.saveRoutineExercises(routineId, exercises, setsMap)
                repository.updateRoutine(routine.copy(updatedAt = System.currentTimeMillis()))
            }
            _state.update { it.copy(showRoutineSaveDialog = false) }
            doFinishWorkout()
        }
    }

    fun showSaveAsNewRoutineDialog() {
        _state.update { it.copy(showRoutineSaveDialog = false, showNewRoutineNameDialog = true) }
    }

    fun dismissNewRoutineNameDialog() {
        _state.update { it.copy(showNewRoutineNameDialog = false) }
        doFinishWorkout()
    }

    fun saveAsNewRoutineAndFinish(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) {
                val (exercises, setsMap) = buildRoutineFromWorkout()
                repository.saveRoutine(trimmed, "", exercises, setsMap)
            }
            _state.update { it.copy(showNewRoutineNameDialog = false) }
            doFinishWorkout()
        }
    }

    private fun buildRoutineFromWorkout(): Pair<List<RoutineExercise>, Map<Long, List<RoutineSet>>> {
        val exercises = mutableListOf<RoutineExercise>()
        val setsMap = mutableMapOf<Long, List<RoutineSet>>()
        _state.value.workoutExercises.forEachIndexed { idx, ex ->
            val localId = (idx + 1).toLong()
            val firstSet = ex.sets.firstOrNull()
            exercises.add(
                RoutineExercise(
                    id = localId,
                    routineId = 0,
                    exerciseId = ex.exerciseId,
                    orderIndex = idx,
                    defaultSets = ex.sets.size.coerceAtLeast(1),
                    defaultReps = firstSet?.reps ?: 10,
                    defaultWeight = firstSet?.weight ?: 0f,
                    restSeconds = firstSet?.restSeconds ?: 90
                )
            )
            setsMap[localId] = ex.sets.mapIndexed { sIdx, s ->
                RoutineSet(
                    routineExerciseId = 0,
                    setNumber = sIdx + 1,
                    weight = s.weight,
                    reps = s.reps,
                    restSeconds = s.restSeconds
                )
            }
        }
        return exercises to setsMap
    }

    private fun doFinishWorkout() {
        cancelRestTimer()
        stopServiceAndClearNotification()
        unregisterWorkoutActionsReceiver()
        _state.update { it.copy(showFinishDialog = false, uncompletedSetsCount = 0) }
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            persistExerciseOrderAndIds()
            repository.finishWorkout(workoutId, _state.value.workoutName)
            _state.update { it.copy(isFinished = true) }
        }
    }

    fun replaceExercise(workoutExerciseId: Long, newExerciseId: Long) {
        viewModelScope.launch {
            val exerciseIndex = _state.value.workoutExercises.indexOfFirst { it.workoutExerciseId == workoutExerciseId }
            if (exerciseIndex == -1) return@launch

            val oldExercise = _state.value.workoutExercises[exerciseIndex]
            val newExercise = repository.getExerciseById(newExerciseId) ?: return@launch
            val previousSets = repository.getPreviousSessionSets(newExerciseId)
                .map { PreviousSetInfo(it.weightKg, it.reps) }
            val defaultRest = _state.value.restTimerSeconds
            val lastSetRest = _state.value.lastSetRestSeconds

            oldExercise.sets.forEach { set ->
                set.setId?.let { id ->
                    repository.deleteSet(
                        SetLog(
                            id = id,
                            workoutExerciseId = workoutExerciseId,
                            setNumber = set.setNumber,
                            weightKg = set.weight,
                            reps = set.reps,
                            setType = set.setType
                        )
                    )
                }
            }

            val setCount = if (previousSets.isNotEmpty()) previousSets.size else oldExercise.sets.size.coerceAtLeast(1)
            val uiSets = mutableListOf<SetUi>()
            for (setIdx in 0 until setCount) {
                val rest = if (setIdx == setCount - 1) lastSetRest else defaultRest
                val setId = repository.logSet(
                    workoutExerciseId, setIdx + 1,
                    previousSets.getOrNull(setIdx)?.weight ?: 0f,
                    previousSets.getOrNull(setIdx)?.reps ?: 0,
                    null, SetType.NORMAL, restSeconds = rest, completedAt = 0
                )
                uiSets.add(
                    SetUi(
                        setId = setId,
                        setNumber = setIdx + 1,
                        weight = previousSets.getOrNull(setIdx)?.weight ?: 0f,
                        reps = previousSets.getOrNull(setIdx)?.reps ?: 0,
                        setType = SetType.NORMAL,
                        restSeconds = rest,
                        previousSetInfo = previousSets.getOrNull(setIdx)
                    )
                )
            }

            val updatedExercises = _state.value.workoutExercises.toMutableList()
            updatedExercises[exerciseIndex] = oldExercise.copy(
                exerciseId = newExerciseId,
                exerciseName = newExercise.name,
                muscleGroup = newExercise.muscleGroup,
                equipment = newExercise.equipment,
                classification = newExercise.classification,
                sets = uiSets,
                previousSets = previousSets
            )
            _state.update { it.copy(workoutExercises = updatedExercises) }
            persistExerciseOrderAndIds()
        }
    }

    fun loadCompletedWorkout(workoutId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(workoutId = workoutId, isViewMode = true) }
            val workout = repository.getWorkoutById(workoutId)
            if (workout != null) {
                _state.update {
                    it.copy(
                        workoutName = workout.workoutName ?: workout.routineName,
                        startTime = workout.startTime,
                        endTime = workout.endTime,
                        sourceRoutineId = workout.routineId
                    )
                }
            }
            loadWorkoutExercises(workoutId)
        }
    }

    fun enterHistoryEditMode() {
        _state.update { it.copy(isViewMode = false, isEditingHistory = true) }
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            loadWorkoutExercises(workoutId, isOngoing = true)
        }
    }

    fun exitHistoryEditMode() {
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            val workout = repository.getWorkoutById(workoutId)
            if (workout != null) {
                val newEndTime = workout.endTime ?: System.currentTimeMillis()
                if (workout.endTime == null) {
                    repository.updateWorkout(workout.copy(endTime = newEndTime, workoutName = _state.value.workoutName))
                } else if (_state.value.workoutName != workout.workoutName) {
                    repository.updateWorkout(workout.copy(workoutName = _state.value.workoutName))
                }
                _state.update { it.copy(endTime = newEndTime) }
            }
            persistExerciseOrderAndIds()
            markAllSetsCompleted()
            _state.update { it.copy(isViewMode = true, isEditingHistory = false) }
            loadWorkoutExercises(workoutId)
        }
    }

    private suspend fun markAllSetsCompleted() {
        val now = System.currentTimeMillis()
        for (exercise in _state.value.workoutExercises) {
            for (set in exercise.sets) {
                if (!set.isCompleted && set.setId != null) {
                    repository.updateSet(
                        SetLog(
                            id = set.setId,
                            workoutExerciseId = exercise.workoutExerciseId,
                            setNumber = set.setNumber,
                            weightKg = set.weight,
                            reps = set.reps,
                            rpe = set.rpe,
                            setType = set.setType,
                            restSeconds = set.restSeconds,
                            completedAt = now
                        )
                    )
                }
            }
        }
        // Reflect the new completion state in memory so subsequent edits don't re-stamp completedAt.
        _state.update { state ->
            state.copy(
                workoutExercises = state.workoutExercises.map { ex ->
                    ex.copy(sets = ex.sets.map { s ->
                        if (!s.isCompleted) s.copy(isCompleted = true, completedAt = now) else s
                    })
                }
            )
        }
    }

    fun updateWorkoutStartTime(newStartMillis: Long) {
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            val workout = repository.getWorkoutById(workoutId) ?: return@launch
            val durationMs = (workout.endTime ?: System.currentTimeMillis()) - workout.startTime
            val newEnd = if (workout.endTime != null) newStartMillis + durationMs else workout.endTime
            repository.updateWorkout(workout.copy(startTime = newStartMillis, endTime = newEnd))
            _state.update { it.copy(startTime = newStartMillis, endTime = newEnd) }
        }
    }

    fun cancelWorkout(onCancelled: () -> Unit) {
        cancelRestTimer()
        stopServiceAndClearNotification()
        unregisterWorkoutActionsReceiver()
        viewModelScope.launch {
            val workoutId = _state.value.workoutId ?: return@launch
            val workout = repository.getWorkoutById(workoutId)
            if (workout != null) {
                repository.deleteWorkout(workout)
            }
            _state.value = ActiveWorkoutState()
            onCancelled()
        }
    }

    fun saveExerciseNote(exerciseId: Long, noteText: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.upsertNote(ExerciseNote(exerciseId = exerciseId, noteText = noteText, updatedAt = now))
            _state.update { state ->
                state.copy(
                    workoutExercises = state.workoutExercises.map { we ->
                        if (we.exerciseId == exerciseId) we.copy(noteText = noteText) else we
                    }
                )
            }
        }
    }

    fun createCustomExercise(exercise: Exercise, onCreated: (Exercise) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertCustomExercise(exercise)
            onCreated(exercise.copy(id = id))
        }
    }
}
