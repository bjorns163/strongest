package com.strongest.app.utils

import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.model.SetType

enum class PrKind { WEIGHT, ONE_RM, VOLUME }

data class WorkoutPrInfo(
    val kind: PrKind,
    val exerciseId: Long? = null,
    val exerciseName: String? = null,
    val muscleGroup: String? = null,
    val weightKg: Float? = null,
    val reps: Int? = null,
    val oneRmKg: Float? = null,
    val volumeKg: Float? = null
)

fun epleyOneRm(weightKg: Float, reps: Int): Float {
    if (reps <= 0 || weightKg <= 0f) return 0f
    return weightKg * (1f + reps / 30f)
}

fun computeWorkoutVolume(rows: List<HistorySetRow>): Float {
    var volume = 0f
    for (r in rows) {
        val w = r.weightKg ?: continue
        val reps = r.reps ?: 0
        volume += w * reps
    }
    return volume
}

/**
 * Warm-up sets do not count toward any statistic — every Progress query filters them out in SQL.
 * The rows feeding PRs come from `getAllCompletedHistoryRows`, which deliberately returns
 * everything, so the same rule is applied here rather than relying on the caller.
 */
fun List<HistorySetRow>.excludingWarmUps(): List<HistorySetRow> =
    filter { it.setType != SetType.WARM_UP.name }

fun computeWorkoutPrs(rows: List<HistorySetRow>, workoutId: Long): List<WorkoutPrInfo> {
    val allRows = rows.excludingWarmUps()
    if (allRows.isEmpty()) return emptyList()

    val workoutVolume = mutableMapOf<Long, Float>()
    val byWorkout = allRows.groupBy { it.workoutId }
    for ((wid, wrows) in byWorkout) {
        workoutVolume[wid] = computeWorkoutVolume(wrows)
    }
    val maxVolumeWorkoutId = workoutVolume.entries.maxByOrNull { it.value }?.key

    val maxWeightByExercise = mutableMapOf<Long, Float>()
    val maxOneRmByExercise = mutableMapOf<Long, Float>()
    for (r in allRows) {
        val w = r.weightKg ?: continue
        val reps = r.reps ?: 0
        val exId = r.exerciseId
        val curW = maxWeightByExercise[exId] ?: 0f
        if (w > curW) maxWeightByExercise[exId] = w
        val orm = epleyOneRm(w, reps)
        val curOrm = maxOneRmByExercise[exId] ?: 0f
        if (orm > curOrm) maxOneRmByExercise[exId] = orm
    }

    val thisRows = byWorkout[workoutId].orEmpty()
    val bestPerExerciseInWorkout = thisRows.groupBy { it.exerciseId }
    val prs = mutableListOf<WorkoutPrInfo>()

    for ((exId, rows) in bestPerExerciseInWorkout) {
        val name = rows.firstOrNull()?.exerciseName
        val muscleGroup = rows.firstOrNull()?.muscleGroup
        val isCardio = muscleGroup == "CARDIO"
        var bestW = 0f
        var bestReps = 0
        var bestOrm = 0f
        for (r in rows) {
            val w = r.weightKg ?: continue
            val reps = r.reps ?: 0
            if (w > bestW || (w == bestW && reps > bestReps)) {
                bestW = w
                bestReps = reps
            }
            val orm = epleyOneRm(w, reps)
            if (orm > bestOrm) bestOrm = orm
        }
        val globalMaxW = maxWeightByExercise[exId] ?: 0f
        if (bestW > 0f && bestW >= globalMaxW) {
            prs.add(WorkoutPrInfo(PrKind.WEIGHT, exerciseId = exId, exerciseName = name, muscleGroup = muscleGroup, weightKg = bestW, reps = bestReps))
        }
        val globalMaxOrm = maxOneRmByExercise[exId] ?: 0f
        if (bestOrm > 0f && bestOrm >= globalMaxOrm && !isCardio) {
            prs.add(WorkoutPrInfo(PrKind.ONE_RM, exerciseId = exId, exerciseName = name, muscleGroup = muscleGroup, oneRmKg = bestOrm))
        }
    }

    if (workoutId == maxVolumeWorkoutId && (workoutVolume[workoutId] ?: 0f) > 0f) {
        prs.add(WorkoutPrInfo(PrKind.VOLUME, volumeKg = workoutVolume[workoutId]))
    }

    return prs
}
