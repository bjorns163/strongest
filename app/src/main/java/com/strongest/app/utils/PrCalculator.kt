package com.strongest.app.utils

import com.strongest.app.data.db.HistorySetRow
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.SetType
import kotlin.math.roundToInt

/**
 * The kinds of record, per exercise unless noted. An exercise's records are listed in
 * [EXERCISE_PR_PRIORITY] order. [VOLUME] is the whole-workout total.
 */
enum class PrKind {
    /** Heaviest weight lifted. */
    WEIGHT,
    /** Best estimated one-rep max, from sets of at most [ONE_RM_MAX_REPS] reps. */
    ONE_RM,
    /** More reps than ever before at a weight that has been lifted before. */
    REPS_AT_WEIGHT,
    /** Most reps in a set without added weight (push-ups, pull-ups, …). */
    MAX_REPS,
    /** Best single set by weight × reps. */
    SET_VOLUME,
    /** Most weight × reps for this exercise in one workout. */
    EXERCISE_VOLUME,
    /** Longest cardio set. */
    CARDIO_DURATION,
    /** Highest cardio machine level. */
    CARDIO_LEVEL,
    /** Most weight × reps in one workout, across all exercises. */
    VOLUME
}

private val EXERCISE_PR_PRIORITY = listOf(
    PrKind.WEIGHT, PrKind.ONE_RM, PrKind.REPS_AT_WEIGHT, PrKind.MAX_REPS,
    PrKind.SET_VOLUME, PrKind.EXERCISE_VOLUME,
    PrKind.CARDIO_DURATION, PrKind.CARDIO_LEVEL
)

/** Epley overestimates badly past about a dozen reps; higher-rep sets don't count toward 1RM. */
const val ONE_RM_MAX_REPS = 12

data class WorkoutPrInfo(
    val kind: PrKind,
    val exerciseId: Long? = null,
    val exerciseName: String? = null,
    val muscleGroup: String? = null,
    /** Weight lifted, or the machine level for cardio. */
    val weightKg: Float? = null,
    /** Reps, or the duration in seconds for cardio. */
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
 * Weights are stored in kg, so a weight entered in lbs comes back with float noise. Rounding to
 * 10 g makes the same plate load land on the same key every time.
 */
private fun weightKey(weightKg: Float): Int = (weightKg * 100f).roundToInt()

/** Running all-time bests for one exercise. */
private class ExerciseBests {
    var weight = 0f
    var oneRm = 0f
    var maxReps = 0
    var setVolume = 0f
    var exerciseVolume = 0f
    var cardioSeconds = 0
    var cardioLevel = 0f
    val repsAtWeight = mutableMapOf<Int, Int>()
}

/**
 * The PRs of every workout in [rows], keyed by workout id, in one chronological pass. Workouts are
 * ordered by start time, with the id breaking ties (and covering rows without a start time).
 */
fun computeAllWorkoutPrs(rows: List<HistorySetRow>): Map<Long, List<WorkoutPrInfo>> {
    val allRows = rows.excludingWarmUps()
    if (allRows.isEmpty()) return emptyMap()

    val workouts = allRows.groupBy { it.workoutId }.entries
        .sortedWith(compareBy({ it.value.first().workoutStartTime }, { it.key }))

    val bestsByExercise = mutableMapOf<Long, ExerciseBests>()
    var maxVolume = 0f
    val result = mutableMapOf<Long, List<WorkoutPrInfo>>()

    for ((workoutId, workoutRows) in workouts) {
        val prs = mutableListOf<WorkoutPrInfo>()
        for ((exId, exRows) in workoutRows.groupBy { it.exerciseId }) {
            val first = exRows.first()
            val bests = bestsByExercise.getOrPut(exId) { ExerciseBests() }
            val sets = exRows.mapNotNull { r ->
                val reps = r.reps ?: return@mapNotNull null
                if (reps <= 0) null else (r.weightKg ?: 0f) to reps
            }
            val candidates = if (first.muscleGroup == MuscleGroup.CARDIO.name) {
                cardioPrs(sets, bests)
            } else {
                strengthPrs(sets, bests)
            }
            candidates.sortedBy { EXERCISE_PR_PRIORITY.indexOf(it.kind) }
                .mapTo(prs) { it.copy(exerciseId = exId, exerciseName = first.exerciseName, muscleGroup = first.muscleGroup) }
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

/**
 * Every record [sets] (weight to reps) beat, updating [bests] as it goes. Records are checked
 * against the bests from before this workout, so two sets in one workout can't PR off each other.
 */
private fun strengthPrs(sets: List<Pair<Float, Int>>, bests: ExerciseBests): List<WorkoutPrInfo> {
    val prs = mutableListOf<WorkoutPrInfo>()
    val loaded = sets.filter { it.first > 0f }
    val bodyweight = sets.filter { it.first <= 0f }

    loaded.maxWithOrNull(compareBy({ it.first }, { it.second }))?.let { (w, reps) ->
        if (w > bests.weight) {
            prs.add(WorkoutPrInfo(PrKind.WEIGHT, weightKg = w, reps = reps))
            bests.weight = w
        }
    }

    loaded.filter { it.second <= ONE_RM_MAX_REPS }
        .maxOfOrNull { epleyOneRm(it.first, it.second) }
        ?.let { orm ->
            if (orm > bests.oneRm) {
                prs.add(WorkoutPrInfo(PrKind.ONE_RM, oneRmKg = orm))
                bests.oneRm = orm
            }
        }

    // A weight lifted for the first time sets a baseline, not a reps record: otherwise every new
    // weight would be a PR. Report the heaviest weight that improved.
    val repsByWeight = loaded.groupBy { weightKey(it.first) }
        .mapValues { (_, s) -> s.maxBy { it.second } }
    repsByWeight.entries
        .filter { (key, set) -> bests.repsAtWeight[key]?.let { set.second > it } == true }
        .maxByOrNull { it.value.first }
        ?.let { (_, set) -> prs.add(WorkoutPrInfo(PrKind.REPS_AT_WEIGHT, weightKg = set.first, reps = set.second)) }
    for ((key, set) in repsByWeight) {
        bests.repsAtWeight[key] = maxOf(bests.repsAtWeight[key] ?: 0, set.second)
    }

    bodyweight.maxOfOrNull { it.second }?.let { reps ->
        if (reps > bests.maxReps) {
            prs.add(WorkoutPrInfo(PrKind.MAX_REPS, reps = reps))
            bests.maxReps = reps
        }
    }

    loaded.maxByOrNull { it.first * it.second }?.let { (w, reps) ->
        val volume = w * reps
        if (volume > bests.setVolume) {
            prs.add(WorkoutPrInfo(PrKind.SET_VOLUME, weightKg = w, reps = reps, volumeKg = volume))
            bests.setVolume = volume
        }
    }

    val exerciseVolume = loaded.sumOf { (it.first * it.second).toDouble() }.toFloat()
    if (exerciseVolume > bests.exerciseVolume) {
        prs.add(WorkoutPrInfo(PrKind.EXERCISE_VOLUME, volumeKg = exerciseVolume))
        bests.exerciseVolume = exerciseVolume
    }
    return prs
}

/** Cardio keeps the machine level in the weight column and the duration in seconds in reps. */
private fun cardioPrs(sets: List<Pair<Float, Int>>, bests: ExerciseBests): List<WorkoutPrInfo> {
    val prs = mutableListOf<WorkoutPrInfo>()
    sets.maxWithOrNull(compareBy({ it.second }, { it.first }))?.let { (level, seconds) ->
        if (seconds > bests.cardioSeconds) {
            prs.add(WorkoutPrInfo(PrKind.CARDIO_DURATION, weightKg = level, reps = seconds))
            bests.cardioSeconds = seconds
        }
    }
    sets.filter { it.first > 0f }.maxWithOrNull(compareBy({ it.first }, { it.second }))?.let { (level, seconds) ->
        if (level > bests.cardioLevel) {
            prs.add(WorkoutPrInfo(PrKind.CARDIO_LEVEL, weightKg = level, reps = seconds))
            bests.cardioLevel = level
        }
    }
    return prs
}
