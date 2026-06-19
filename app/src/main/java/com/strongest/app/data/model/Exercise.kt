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
    val classification: ExerciseClassification = ExerciseClassification.ISOLATION
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

enum class ExerciseClassification {
    COMPOUND, ACCESSORY, ISOLATION;

    fun label(): String = when (this) {
        COMPOUND -> "Compound"
        ACCESSORY -> "Accessory"
        ISOLATION -> "Isolation"
    }
}

fun classifyExercise(name: String, equipment: Equipment, muscleGroup: MuscleGroup): ExerciseClassification {
    val n = name.lowercase()
    fun any(vararg needles: String): Boolean = needles.any { n.contains(it) }

    // Specific isolation keywords first (single-joint movements).
    if (any(
            "lateral raise", "front raise", "rear delt", "reverse fly",
            "fly", "flye",
            "curl",
            "extension",
            "kickback", "pushdown",
            "calf raise", "calf press",
            "crunch", "sit-up", "sit up", "leg raise", "knee raise",
            "shrug",
            "pec deck", "pec fly",
            "pullover",
            "face pull",
            "wrist curl", "wrist extension"
        )
    ) return ExerciseClassification.ISOLATION

    // Big compound lifts.
    if (any("deadlift") && !any("romanian deadlift", "rdl", "stiff-leg deadlift", "stiff leg deadlift"))
        return ExerciseClassification.COMPOUND
    if (any("clean", "snatch", "jerk", "thruster")) return ExerciseClassification.COMPOUND
    if (any("pull-up", "pull up", "pullup", "chin-up", "chin up", "chinup", "muscle-up", "muscle up"))
        return ExerciseClassification.COMPOUND
    if (any("dip") && !any("dip machine assisted")) return ExerciseClassification.COMPOUND
    if (any("squat") && !any("hack squat", "split squat", "goblet squat", "pistol squat", "bulgarian"))
        return ExerciseClassification.COMPOUND
    if (any("barbell row", "pendlay row", "bent over row", "bent-over row", "yates row", "t-bar row", "t bar row"))
        return ExerciseClassification.COMPOUND
    if (equipment == Equipment.BARBELL && any("bench press", "overhead press", "military press", "push press"))
        return ExerciseClassification.COMPOUND

    // Accessory: multi-joint movements that aren't the primary compound lift.
    if (any(
            "press", "row", "lunge",
            "hip thrust", "glute bridge",
            "hyperextension", "back extension",
            "romanian deadlift", "rdl", "stiff-leg deadlift", "stiff leg deadlift",
            "hack squat", "split squat", "bulgarian", "pistol squat", "goblet squat",
            "push-up", "push up", "pushup",
            "leg press",
            "good morning",
            "step-up", "step up"
        )
    ) return ExerciseClassification.ACCESSORY

    if (muscleGroup == MuscleGroup.FULL_BODY) return ExerciseClassification.COMPOUND

    return ExerciseClassification.ISOLATION
}
