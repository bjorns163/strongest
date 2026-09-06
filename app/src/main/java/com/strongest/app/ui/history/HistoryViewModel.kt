package com.strongest.app.ui.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.model.Workout
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.utils.Cell
import com.strongest.app.utils.WorkoutPrInfo
import com.strongest.app.utils.XlsxWriter
import com.strongest.app.utils.computeWorkoutPrs
import com.strongest.app.utils.excludingWarmUps
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.weightUnitLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HistoryExerciseSummary(
    val exerciseId: Long,
    val exerciseName: String,
    val setCount: Int,
    val bestWeightKg: Float,
    val bestReps: Int,
    val muscleGroup: String = ""
)

data class WorkoutSummary(
    val workoutId: Long,
    val totalVolumeKg: Float,
    val exercises: List<HistoryExerciseSummary>,
    val prs: List<WorkoutPrInfo>
)

/** Source-routine details shown on a history card, when the workout came from a routine. */
data class RoutineInfo(val description: String, val groupName: String?)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    val workouts: StateFlow<List<Workout>> = repository.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val summaries: StateFlow<Map<Long, WorkoutSummary>> =
        repository.getAllCompletedHistoryRows()
            .combine(workouts) { rows, _ -> buildSummaries(rows) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    /** workoutId -> source routine's description + group name (only for routine-derived workouts). */
    val routineInfo: StateFlow<Map<Long, RoutineInfo>> =
        workouts
            .map { list -> buildRoutineInfo(list) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private suspend fun buildRoutineInfo(workouts: List<Workout>): Map<Long, RoutineInfo> {
        val byWorkout = mutableMapOf<Long, RoutineInfo>()
        val byRoutine = mutableMapOf<Long, RoutineInfo?>()
        for (w in workouts) {
            val routineId = w.routineId ?: continue
            val info = byRoutine.getOrPut(routineId) {
                val routine = repository.getRoutineById(routineId) ?: return@getOrPut null
                val groupName = routine.groupId?.let { repository.getRoutineGroupById(it)?.name }
                RoutineInfo(description = routine.description, groupName = groupName)
            }
            if (info != null) byWorkout[w.id] = info
        }
        return byWorkout
    }

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            val workout = repository.getWorkoutById(workoutId)
            if (workout != null) {
                repository.deleteWorkout(workout)
            }
        }
    }

    fun enterSelectionMode() {
        _selectionMode.value = true
        _selectedIds.value = emptySet()
    }

    fun exitSelectionMode() {
        _selectionMode.value = false
        _selectedIds.value = emptySet()
    }

    fun toggleSelection(workoutId: Long) {
        _selectedIds.update { current ->
            if (workoutId in current) current - workoutId else current + workoutId
        }
    }

    fun selectAll() {
        val ids = workouts.value.filter { !it.isOngoing }.map { it.id }.toSet()
        _selectedIds.value = ids
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun shareSelectedWorkouts(context: Context, weightUnit: WeightUnit) {
        viewModelScope.launch {
            val ids = _selectedIds.value
            if (ids.isEmpty()) return@launch

            val uri = withContext(Dispatchers.IO) {
                buildExportFile(context, ids, weightUnit)
            } ?: return@launch
            startShareIntent(context, uri)
            exitSelectionMode()
        }
    }

    private suspend fun buildExportFile(
        context: Context,
        ids: Set<Long>,
        weightUnit: WeightUnit
    ): android.net.Uri? {
        val header = listOf(
                "Date", "Workout Name", "Routine", "Duration (min)",
                "Exercise", "Muscle Group", "Equipment",
                "Set #", "Weight (${weightUnitLabel(weightUnit)})", "Reps", "RPE", "Set Type", "Completed At"
            )

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val rows = mutableListOf<List<Cell>>()
            val sortedIds = workouts.value.filter { it.id in ids && !it.isOngoing }
                .sortedByDescending { it.startTime }
                .map { it.id }

            for (workoutId in sortedIds) {
                val details = repository.getWorkoutWithDetails(workoutId) ?: continue
                val workout = details.workout
                val durationMin = if (workout.endTime != null) {
                    ((workout.endTime - workout.startTime) / 60000).toDouble()
                } else 0.0

                if (details.exercises.isEmpty()) {
                    rows.add(listOf(
                        Cell.Str(dateFormat.format(Date(workout.startTime))),
                        Cell.Str(workout.workoutName ?: workout.routineName ?: ""),
                        Cell.Str(workout.routineName ?: ""),
                        Cell.Num(durationMin),
                        Cell.Str(""), Cell.Str(""), Cell.Str(""),
                        Cell.Str(""), Cell.Str(""), Cell.Str(""),
                        Cell.Str(""), Cell.Str(""), Cell.Str("")
                    ))
                    continue
                }

                for (ex in details.exercises) {
                    val exercise = repository.getExerciseById(ex.workoutExercise.exerciseId)
                    val exName = exercise?.name ?: "Unknown"
                    val muscle = exercise?.muscleGroup?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
                    val equipment = exercise?.equipment?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""

                    if (ex.sets.isEmpty()) {
                        rows.add(listOf(
                            Cell.Str(dateFormat.format(Date(workout.startTime))),
                            Cell.Str(workout.workoutName ?: workout.routineName ?: ""),
                            Cell.Str(workout.routineName ?: ""),
                            Cell.Num(durationMin),
                            Cell.Str(exName),
                            Cell.Str(muscle),
                            Cell.Str(equipment),
                            Cell.Str(""), Cell.Str(""), Cell.Str(""),
                            Cell.Str(""), Cell.Str(""), Cell.Str("")
                        ))
                        continue
                    }

                    for (set in ex.sets) {
                        val displayWeight = kgToDisplay(set.weightKg, weightUnit)
                        rows.add(listOf(
                            Cell.Str(dateFormat.format(Date(workout.startTime))),
                            Cell.Str(workout.workoutName ?: workout.routineName ?: ""),
                            Cell.Str(workout.routineName ?: ""),
                            Cell.Num(durationMin),
                            Cell.Str(exName),
                            Cell.Str(muscle),
                            Cell.Str(equipment),
                            Cell.Num(set.setNumber.toDouble()),
                            Cell.Num(displayWeight.toDouble()),
                            Cell.Num(set.reps.toDouble()),
                            if (set.rpe != null) Cell.Num(set.rpe.toDouble()) else Cell.Str(""),
                            Cell.Str(set.setType.name),
                            Cell.Str(if (set.completedAt > 0) timeFormat.format(Date(set.completedAt)) else "")
                        ))
                    }
                }
            }

        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val filename = "strongest_history_${System.currentTimeMillis()}.xlsx"
        val file = File(exportsDir, filename)
        file.outputStream().use { out ->
            XlsxWriter.write(out, "Workout History", header, rows)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun startShareIntent(context: Context, uri: android.net.Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Strongest Workout History")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Workout History").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

private fun buildSummaries(rows: List<HistorySetRow>): Map<Long, WorkoutSummary> {
    if (rows.isEmpty()) return emptyMap()

    // Warm-ups are excluded from the card's volume and set count so History agrees with the
    // Progress tab, which filters them in SQL. The workout detail view still lists them.
    val workingRows = rows.excludingWarmUps()
    val byWorkout = workingRows.groupBy { it.workoutId }
    val result = mutableMapOf<Long, WorkoutSummary>()

    for ((workoutId, wrows) in byWorkout) {
        var volume = 0f
        val bestPerEx = mutableMapOf<Long, HistoryExerciseSummary>()
        for ((exId, exRows) in wrows.groupBy { it.exerciseId }) {
            val name = exRows.first().exerciseName
            val setCount = exRows.count { it.setId != null }
            // Skip exercises with no completed sets (e.g. all sets discarded on finish).
            if (setCount == 0) continue
            var bestW = 0f
            var bestReps = 0
            for (r in exRows) {
                val w = r.weightKg ?: continue
                val reps = r.reps ?: 0
                volume += w * reps
                if (w > bestW || (w == bestW && reps > bestReps)) {
                    bestW = w
                    bestReps = reps
                }
            }
            bestPerEx[exId] = HistoryExerciseSummary(
                exerciseId = exId,
                exerciseName = name,
                setCount = setCount,
                bestWeightKg = bestW,
                bestReps = bestReps,
                muscleGroup = exRows.first().muscleGroup
            )
        }

        result[workoutId] = WorkoutSummary(
            workoutId = workoutId,
            totalVolumeKg = volume,
            exercises = bestPerEx.values.sortedBy { it.exerciseName },
            prs = computeWorkoutPrs(workingRows, workoutId)
        )
    }
    return result
}
