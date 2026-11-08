package com.strongest.app.utils

import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.SetType
import com.strongest.app.data.repository.WeightUnit
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Progressive-overload suggestions: given how an exercise went the last time(s), what to load
 * and how many reps to aim for today.
 *
 * Everything here is pure Kotlin so the rules can be unit-tested without a device; the
 * ViewModel gathers the history and settings and the screen only renders the result.
 */

/** How an exercise progresses. Stored per exercise; null there means [DOUBLE]. */
enum class ProgressionMethod {
    /** Work up through a rep range at one weight, then add weight and start at the bottom again. */
    DOUBLE,

    /** Hit the top of the range on every set → add weight; keep the reps the same. */
    LINEAR,

    /** No suggestions for this exercise. */
    OFF
}

/** The global knobs from Settings → Progression. */
data class ProgressionConfig(
    val enabled: Boolean = true,
    val useRpe: Boolean = true,
    val defaultRepMin: Int = 8,
    val defaultRepMax: Int = 12,
    val defaultTargetRpe: Int = 8,
    /** After this many days since the exercise was last done, don't push; ease back in. */
    val longBreakDays: Int = 21,
    /** How much lighter to go after a failed session (percent). */
    val failDropPercent: Int = 5,
    /** Same weight this many sessions in a row without progress → suggest a deload. 0 = off. */
    val plateauSessions: Int = 3,
    /** How much lighter a deload is (percent). */
    val deloadPercent: Int = 10,
    /** Write the suggestion into the set fields instead of only showing it. */
    val autoFill: Boolean = true
) {
    companion object {
        val REP_RANGE_LIMITS = 1..50
        val TARGET_RPE_OPTIONS = listOf(7, 8, 9)
        val LONG_BREAK_LIMITS = 7..60
        val FAIL_DROP_LIMITS = 0..15
        val PLATEAU_LIMITS = 0..6
        val DELOAD_LIMITS = 5..20
    }
}

/** What the routine (or the defaults) asks for on this exercise. */
data class ProgressionTarget(
    val repMin: Int,
    val repMax: Int,
    val targetRpe: Int
)

/** One logged set, as far as progression cares. */
data class ProgressionSet(
    val weightKg: Float,
    val reps: Int,
    val rpe: Float?,
    val setType: SetType
)

/** One past session of a single exercise. */
data class ProgressionSession(
    val workoutId: Long,
    val routineId: Long?,
    val startTime: Long,
    val sets: List<ProgressionSet>
)

enum class SuggestionKind {
    /** Add one weight step and start at the bottom of the rep range. */
    INCREASE_WEIGHT,

    /** Same weight, aim for more reps. */
    ADD_REPS,

    /** Same weight, same reps: consolidate before pushing. */
    REPEAT,

    /** Last session failed: go a bit lighter. */
    DECREASE,

    /** Stuck at the same weight for a while: back off to build up again. */
    DELOAD,

    /** Long time since this exercise: ease back in. */
    LONG_BREAK
}

/** Summary of the session a suggestion was based on, for the "why" line under it. */
data class SuggestionBasis(
    val weightKg: Float,
    val sets: Int,
    /** Lowest rep count across the top-weight working sets. */
    val reps: Int,
    val avgRpe: Float?,
    val daysAgo: Int,
    /** Sessions in a row at this weight (only meaningful for [SuggestionKind.DELOAD]). */
    val stalledSessions: Int = 0
)

data class ProgressionSuggestion(
    val kind: SuggestionKind,
    val weightKg: Float,
    val reps: Int,
    /** The weight the suggestion moved from; equal to [weightKg] when it holds. */
    val previousWeightKg: Float,
    val previousReps: Int,
    val basis: SuggestionBasis,
    /** Which rep range / RPE the suggestion aimed for. */
    val target: ProgressionTarget
)

private const val PROGRESSION_DAY_MS = 24L * 60 * 60 * 1000
private const val WEIGHT_EPSILON = 0.01f

/**
 * Picks the sessions a suggestion should follow: the ones done in the same routine when there
 * are any, so a heavy day and a light day for the same lift each keep their own track.
 * Otherwise (an empty workout, or the first time in this routine) every session counts.
 * Sessions come back most recent first and without any that had no working sets.
 */
fun selectProgressionTrack(
    sessions: List<ProgressionSession>,
    routineId: Long?
): List<ProgressionSession> {
    val usable = sessions
        .filter { s -> s.sets.any { it.isWorking() } }
        .sortedByDescending { it.startTime }
    if (routineId == null) return usable
    val sameRoutine = usable.filter { it.routineId == routineId }
    return sameRoutine.ifEmpty { usable }
}

/**
 * The rep range to aim for when the routine doesn't set one. The default range is used when
 * [plannedReps] (what the routine or last session used) falls inside it; otherwise a narrow
 * range starting at [plannedReps], so a 5×5 lifter isn't told to work up to 8.
 */
fun implicitProgressionTarget(plannedReps: Int?, config: ProgressionConfig): ProgressionTarget {
    val min = config.defaultRepMin
    val maxReps = max(config.defaultRepMax, min)
    if (plannedReps == null || plannedReps <= 0 || plannedReps in min..maxReps) {
        return ProgressionTarget(min, maxReps, config.defaultTargetRpe)
    }
    val width = ((maxReps - min) / 2).coerceAtLeast(1)
    return ProgressionTarget(plannedReps, plannedReps + width, config.defaultTargetRpe)
}

/**
 * Default weight step for an equipment type, in kg. 0 means progress by reps only
 * (bodyweight, bands); when such an exercise is loaded anyway (weighted dips), a small
 * plate step is used.
 */
fun defaultWeightStepKg(equipment: Equipment, unit: WeightUnit, currentWeightKg: Float): Float {
    val stepInUnit: Float = when (unit) {
        WeightUnit.KG -> when (equipment) {
            Equipment.BARBELL, Equipment.EZ_BAR, Equipment.TRAP_BAR,
            Equipment.SMITH_MACHINE, Equipment.PLATE -> 2.5f
            Equipment.DUMBBELL -> 2f
            Equipment.MACHINE, Equipment.CABLE -> 5f
            Equipment.KETTLEBELL -> 4f
            Equipment.MEDICINE_BALL -> 1f
            Equipment.BODYWEIGHT, Equipment.RESISTANCE_BAND, Equipment.SUSPENSION,
            Equipment.NONE, Equipment.OTHER -> if (currentWeightKg > 0f) 2.5f else 0f
        }
        WeightUnit.LBS -> when (equipment) {
            Equipment.BARBELL, Equipment.EZ_BAR, Equipment.TRAP_BAR,
            Equipment.SMITH_MACHINE, Equipment.PLATE, Equipment.DUMBBELL -> 5f
            Equipment.MACHINE, Equipment.CABLE -> 10f
            Equipment.KETTLEBELL -> 9f
            Equipment.MEDICINE_BALL -> 2f
            Equipment.BODYWEIGHT, Equipment.RESISTANCE_BAND, Equipment.SUSPENSION,
            Equipment.NONE, Equipment.OTHER -> if (currentWeightKg > 0f) 5f else 0f
        }
    }
    return displayToKg(stepInUnit, unit)
}

/** Warm-ups and drop sets don't say anything about whether the working weight was right. */
private fun ProgressionSet.isWorking(): Boolean =
    (setType == SetType.NORMAL || setType == SetType.FAILURE) && reps > 0

/** The top-weight working sets of a session: the ones the weight decision is about. */
private fun ProgressionSession.topSets(): List<ProgressionSet> {
    val working = sets.filter { it.isWorking() }
    val top = working.maxOfOrNull { it.weightKg } ?: return emptyList()
    return working.filter { abs(it.weightKg - top) < WEIGHT_EPSILON }
}

private enum class Effort { EASY, ON_TARGET, MAX, UNKNOWN }

private fun effortOf(avgRpe: Float?, targetRpe: Int): Effort = when {
    avgRpe == null -> Effort.UNKNOWN
    avgRpe >= 9.5f -> Effort.MAX
    avgRpe <= targetRpe - 1f -> Effort.EASY
    else -> Effort.ON_TARGET
}

/** Rounds a reduction to whole weight steps (at least one), so the result is loadable. */
private fun reduce(weightKg: Float, percent: Int, stepKg: Float): Float {
    if (percent <= 0 || weightKg <= 0f) return weightKg
    val drop = weightKg * percent / 100f
    if (stepKg <= 0f) return (weightKg - drop).coerceAtLeast(0f)
    val steps = max(1, (drop / stepKg).roundToInt())
    return (weightKg - steps * stepKg).coerceAtLeast(0f)
}

private fun reduceReps(reps: Int, percent: Int): Int =
    max(1, floor(reps * (1f - percent / 100f)).toInt())

/**
 * Works out today's suggestion, or null when there is nothing to go on (no history, or the
 * method is [ProgressionMethod.OFF]).
 *
 * @param track sessions on this exercise's track, most recent first (see [selectProgressionTrack]).
 * @param stepKg how much one weight increase is; 0 means progress by reps only.
 */
fun suggestProgression(
    track: List<ProgressionSession>,
    method: ProgressionMethod,
    target: ProgressionTarget,
    stepKg: Float,
    config: ProgressionConfig,
    now: Long
): ProgressionSuggestion? {
    if (!config.enabled || method == ProgressionMethod.OFF) return null
    val last = track.firstOrNull() ?: return null
    val top = last.topSets()
    if (top.isEmpty()) return null

    val repMin = target.repMin.coerceAtLeast(1)
    val repMax = target.repMax.coerceAtLeast(repMin)
    val weight = top.first().weightKg
    val reps = top.minOf { it.reps }
    val rpes = if (config.useRpe) top.mapNotNull { it.rpe } else emptyList()
    val avgRpe = if (rpes.isEmpty()) null else rpes.average().toFloat()
    val effort = effortOf(avgRpe, target.targetRpe)
    val daysAgo = ((now - last.startTime) / PROGRESSION_DAY_MS).toInt().coerceAtLeast(0)
    val basis = SuggestionBasis(weight, top.size, reps, avgRpe, daysAgo)
    val repsOnly = stepKg <= 0f

    fun make(kind: SuggestionKind, w: Float, r: Int, b: SuggestionBasis = basis) =
        ProgressionSuggestion(kind, w, r, weight, reps, b, ProgressionTarget(repMin, repMax, target.targetRpe))

    if (daysAgo >= config.longBreakDays) {
        return if (repsOnly) make(SuggestionKind.LONG_BREAK, weight, reduceReps(reps, config.failDropPercent))
        else make(SuggestionKind.LONG_BREAK, reduce(weight, config.failDropPercent, stepKg), reps)
    }

    val hitTop = top.all { it.reps >= repMax }
    val belowRange = reps < repMin

    val base: ProgressionSuggestion = when {
        hitTop && effort == Effort.MAX -> make(SuggestionKind.REPEAT, weight, reps)
        hitTop && repsOnly -> make(SuggestionKind.ADD_REPS, weight, reps + 1)
        hitTop -> make(
            SuggestionKind.INCREASE_WEIGHT,
            weight + stepKg,
            if (method == ProgressionMethod.LINEAR) repMax else repMin
        )
        belowRange && (effort == Effort.MAX || effort == Effort.UNKNOWN) ->
            if (repsOnly) make(SuggestionKind.REPEAT, weight, reps)
            else make(SuggestionKind.DECREASE, reduce(weight, config.failDropPercent, stepKg), repMin)
        method == ProgressionMethod.LINEAR -> make(SuggestionKind.REPEAT, weight, repMax)
        belowRange -> make(SuggestionKind.ADD_REPS, weight, repMin)
        effort == Effort.MAX -> make(SuggestionKind.REPEAT, weight, reps)
        effort == Effort.EASY -> make(SuggestionKind.ADD_REPS, weight, (reps + 2).coerceAtMost(repMax))
        else -> make(SuggestionKind.ADD_REPS, weight, (reps + 1).coerceAtMost(repMax))
    }

    // A plateau only matters when today's plan isn't already a step forward in weight.
    if (base.kind != SuggestionKind.INCREASE_WEIGHT && config.plateauSessions > 0) {
        val stalled = stalledSessionCount(track)
        if (stalled >= config.plateauSessions) {
            val b = basis.copy(stalledSessions = stalled)
            return if (repsOnly) make(SuggestionKind.DELOAD, weight, reduceReps(reps, config.deloadPercent), b)
            else make(SuggestionKind.DELOAD, reduce(weight, config.deloadPercent, stepKg), reps, b)
        }
    }
    return base
}

/**
 * How many of the latest sessions in a row used the same top weight without the reps going
 * up. A deload changes the weight, so it naturally resets this count.
 */
internal fun stalledSessionCount(track: List<ProgressionSession>): Int {
    val tops = track.map { it.topSets() }.takeWhile { it.isNotEmpty() }
    if (tops.isEmpty()) return 0
    val weight = tops.first().first().weightKg
    val sameWeight = tops.takeWhile { abs(it.first().weightKg - weight) < WEIGHT_EPSILON }
    if (sameWeight.size < 2) return sameWeight.size
    val latestReps = sameWeight.first().minOf { it.reps }
    val oldestReps = sameWeight.last().minOf { it.reps }
    val latestVolume = sameWeight.first().sumOf { it.reps }
    val oldestVolume = sameWeight.last().sumOf { it.reps }
    return if (latestReps <= oldestReps && latestVolume <= oldestVolume) sameWeight.size else 0
}

/**
 * The target for an exercise: the routine's own rep range when it sets one, otherwise one
 * derived from what is planned ([plannedReps]) and the Settings defaults.
 */
fun resolveProgressionTarget(
    repMin: Int?,
    repMax: Int?,
    targetRpe: Int?,
    plannedReps: Int?,
    config: ProgressionConfig
): ProgressionTarget {
    val rpe = targetRpe ?: config.defaultTargetRpe
    return if (repMin != null && repMax != null && repMin > 0) {
        ProgressionTarget(repMin, max(repMin, repMax), rpe)
    } else {
        implicitProgressionTarget(plannedReps, config).copy(targetRpe = rpe)
    }
}

/** Lowest rep count of the top-weight working sets in the latest session of [track]. */
fun lastTopReps(track: List<ProgressionSession>): Int? =
    track.firstOrNull()?.topSets()?.minOfOrNull { it.reps }

/** Working sets (normal and to-failure) are the ones a suggestion fills in. */
fun SetType.takesProgressionSuggestion(): Boolean =
    this == SetType.NORMAL || this == SetType.FAILURE
