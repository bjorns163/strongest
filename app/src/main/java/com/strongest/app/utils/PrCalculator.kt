package com.strongest.app.utils

import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.model.MuscleGroup
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

/**
 * Cardio sets store distance and time in the weight and reps columns, so multiplying them is
 * meaningless — they never count toward volume. The Progress queries apply the same rule in SQL.
 */
fun HistorySetRow.countsTowardVolume(): Boolean = muscleGroup != MuscleGroup.CARDIO.name

fun computeWorkoutVolume(rows: List<HistorySetRow>): Float {
    var volume = 0f
    for (r in rows) {
        if (!r.countsTowardVolume()) continue
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

/**
 * A PR is the first time a best is reached: a workout has to *beat* every workout before it.
 * Matching an earlier best again is not a PR, and a later workout beating this one doesn't take
 * the badge away from it either — it was a record when it was set.
 */
fun computeWorkoutPrs(rows: List<HistorySetRow>, workoutId: Long): List<WorkoutPrInfo> =
    computeAllWorkoutPrs(rows)[workoutId].orEmpty()

/**
 * The PRs of every workout in [rows], keyed by workout id, in one chronological pass. Workouts are
 * ordered by start time, with the id breaking ties (and covering rows without a start time).
 */
fun computeAllWorkoutPrs(rows: List<HistorySetRow>): Map<Long, List<WorkoutPrInfo>> {
    val allRows = rows.excludingWarmUps()
    if (allRows.isEmpty()) return emptyMap()

    val workouts = allRows.groupBy { it.workoutId }.entries
        .sortedWith(compareBy({ it.value.first().workoutStartTime }, { it.key }))

    val maxWeightByExercise = mutableMapOf<Long, Float>()
    val maxOneRmByExercise = mutableMapOf<Long, Float>()
    var maxVolume = 0f
    val result = mutableMapOf<Long, List<WorkoutPrInfo>>()

    for ((workoutId, workoutRows) in workouts) {
        val prs = mutableListOf<WorkoutPrInfo>()
        for ((exId, exRows) in workoutRows.groupBy { it.exerciseId }) {
            val name = exRows.first().exerciseName
            val muscleGroup = exRows.first().muscleGroup
            val isCardio = muscleGroup == MuscleGroup.CARDIO.name
            var bestW = 0f
            var bestReps = 0
            var bestOrm = 0f
            for (r in exRows) {
                val w = r.weightKg ?: continue
                val reps = r.reps ?: 0
                if (w > bestW || (w == bestW && reps > bestReps)) {
                    bestW = w
                    bestReps = reps
                }
                val orm = epleyOneRm(w, reps)
                if (orm > bestOrm) bestOrm = orm
            }
            val previousMaxW = maxWeightByExercise[exId] ?: 0f
            if (bestW > 0f && bestW > previousMaxW) {
                prs.add(WorkoutPrInfo(PrKind.WEIGHT, exerciseId = exId, exerciseName = name, muscleGroup = muscleGroup, weightKg = bestW, reps = bestReps))
                maxWeightByExercise[exId] = bestW
            }
            val previousMaxOrm = maxOneRmByExercise[exId] ?: 0f
            if (bestOrm > previousMaxOrm) {
                if (bestOrm > 0f && !isCardio) {
                    prs.add(WorkoutPrInfo(PrKind.ONE_RM, exerciseId = exId, exerciseName = name, muscleGroup = muscleGroup, oneRmKg = bestOrm))
                }
                maxOneRmByExercise[exId] = bestOrm
            }
        }

        val volume = computeWorkoutVolume(workoutRows)
        if (volume > 0f && volume > maxVolume) {
            prs.add(WorkoutPrInfo(PrKind.VOLUME, volumeKg = volume))
            maxVolume = volume
        }
        result[workoutId] = prs
    }
    return result
}
