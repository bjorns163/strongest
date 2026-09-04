package com.strongest.app.utils

import kotlin.math.roundToInt

object OneRepMaxCalculator {
    fun epley(weight: Float, reps: Int): Float {
        if (reps <= 1) return weight
        return weight * (1 + reps / 30f)
    }

    fun brzycki(weight: Float, reps: Int): Float {
        if (reps <= 1) return weight
        // The denominator becomes 0 at reps == 37 and negative beyond. Brzycki is only meaningful
        // for low-rep sets anyway, so cap the input.
        val cappedReps = reps.coerceAtMost(36)
        return weight * (36f / (37f - cappedReps))
    }

    fun average(weight: Float, reps: Int): Float {
        return (epley(weight, reps) + brzycki(weight, reps)) / 2f
    }
}

/** One suggested warm-up step: a weight in the same unit as the working weight, and a rep count. */
data class WarmUpSuggestion(val weight: Float, val reps: Int)

object WarmUpCalculator {

    /** The most warm-up sets the scheme can ever produce. */
    const val MAX_SETS = 4

    /** Percentage of the working weight, and the reps to do at it. Lightest first. */
    private val SCHEME = listOf(
        0.5f to 8,
        0.7f to 5,
        0.85f to 3,
        0.95f to 2
    )

    /**
     * The warm-up sets leading up to [working] x [reps], each rounded to [increment].
     *
     * A step is dropped when it rounds to zero, reaches the working weight, or lands on the same
     * weight as the step before it — so a light working weight yields fewer sets than [count]
     * asks for. Use [maxAchievable] to find out how many are actually possible before offering
     * the user a choice.
     */
    fun suggest(working: Float, reps: Int, increment: Float, count: Int): List<WarmUpSuggestion> {
        if (working <= 0f || reps <= 0 || count <= 0 || increment <= 0f) return emptyList()
        val result = mutableListOf<WarmUpSuggestion>()
        var lastWeight = 0f
        for ((pct, schemeReps) in SCHEME.take(count)) {
            val rounded = (working * pct / increment).roundToInt() * increment
            if (rounded > 0f && rounded < working && rounded > lastWeight + 0.01f) {
                result.add(WarmUpSuggestion(rounded, minOf(schemeReps, reps)))
                lastWeight = rounded
            }
        }
        return result
    }

    /**
     * How many warm-up sets this working weight can actually support. Taking more of the scheme
     * only ever adds steps, so asking for the maximum gives the ceiling.
     */
    fun maxAchievable(working: Float, reps: Int, increment: Float): Int =
        suggest(working, reps, increment, MAX_SETS).size
}
