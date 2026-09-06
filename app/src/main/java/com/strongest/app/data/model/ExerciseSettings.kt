package com.strongest.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-exercise preferences that outlive a single workout.
 *
 * The plate fields remember how this exercise is loaded — a leg press with no bar and a
 * single loading pin keeps those choices instead of asking again every session.
 * [barWeightKg] is null until the user picks a bar for this exercise; 0 means "no bar".
 */
@Entity(tableName = "exercise_settings")
data class ExerciseSettings(
    @PrimaryKey
    val exerciseId: Long,
    val warmUpSetCount: Int = 3,
    val barWeightKg: Float? = null,
    val plateSingleSide: Boolean = false
)
