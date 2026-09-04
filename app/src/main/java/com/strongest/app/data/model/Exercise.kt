package com.strongest.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: Long,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val description: String = "",
    val instructions: String = "",
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val imageUrl: String = "",
    val isCustom: Boolean = false,
    /** Biomechanical category: how many joints and muscle groups the movement recruits. */
    val type: ExerciseType = ExerciseType.ISOLATION
)

/** Relative contribution a secondary muscle gets vs. the primary muscle in progress tracking. */
const val SECONDARY_MUSCLE_WEIGHT = 0.5f

enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, ABS,
    QUADS, HAMSTRINGS, GLUTES, CALVES, FOREARMS,
    LOWER_BACK, TRAPS, FULL_BODY, CARDIO, STRETCHING,
    OTHER
}

enum class Equipment {
    BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT,
    KETTLEBELL, RESISTANCE_BAND, MEDICINE_BALL, SUSPENSION,
    SMITH_MACHINE, EZ_BAR, TRAP_BAR, PLATE, NONE,
    OTHER
}

/** Biomechanical category — how many joints the movement crosses. */
enum class ExerciseType {
    COMPOUND, ISOLATION, ISOMETRIC;

    fun label(): String = when (this) {
        COMPOUND -> "Compound"
        ISOLATION -> "Isolation"
        ISOMETRIC -> "Isometric"
    }
}
