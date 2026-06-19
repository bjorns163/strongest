package com.strongest.app.utils

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
