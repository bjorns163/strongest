package com.strongest.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.strongest.app.R
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.SetType

/*
 * Display names for model enums. The enum names themselves stay the stable identifiers stored in
 * the database and in export / share files; only these labels are translated.
 */

@get:StringRes
val MuscleGroup.labelRes: Int
    get() = when (this) {
        MuscleGroup.CHEST -> R.string.muscle_chest
        MuscleGroup.BACK -> R.string.muscle_back
        MuscleGroup.SHOULDERS -> R.string.muscle_shoulders
        MuscleGroup.BICEPS -> R.string.muscle_biceps
        MuscleGroup.TRICEPS -> R.string.muscle_triceps
        MuscleGroup.ABS -> R.string.muscle_abs
        MuscleGroup.QUADS -> R.string.muscle_quads
        MuscleGroup.HAMSTRINGS -> R.string.muscle_hamstrings
        MuscleGroup.GLUTES -> R.string.muscle_glutes
        MuscleGroup.CALVES -> R.string.muscle_calves
        MuscleGroup.FOREARMS -> R.string.muscle_forearms
        MuscleGroup.LOWER_BACK -> R.string.muscle_lower_back
        MuscleGroup.TRAPS -> R.string.muscle_traps
        MuscleGroup.FULL_BODY -> R.string.muscle_full_body
        MuscleGroup.CARDIO -> R.string.muscle_cardio
        MuscleGroup.STRETCHING -> R.string.muscle_stretching
        MuscleGroup.OTHER -> R.string.muscle_other
    }

@get:StringRes
val Equipment.labelRes: Int
    get() = when (this) {
        Equipment.BARBELL -> R.string.equipment_barbell
        Equipment.DUMBBELL -> R.string.equipment_dumbbell
        Equipment.MACHINE -> R.string.equipment_machine
        Equipment.CABLE -> R.string.equipment_cable
        Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
        Equipment.KETTLEBELL -> R.string.equipment_kettlebell
        Equipment.RESISTANCE_BAND -> R.string.equipment_resistance_band
        Equipment.MEDICINE_BALL -> R.string.equipment_medicine_ball
        Equipment.SUSPENSION -> R.string.equipment_suspension
        Equipment.SMITH_MACHINE -> R.string.equipment_smith_machine
        Equipment.EZ_BAR -> R.string.equipment_ez_bar
        Equipment.TRAP_BAR -> R.string.equipment_trap_bar
        Equipment.PLATE -> R.string.equipment_plate
        Equipment.NONE -> R.string.equipment_none
        Equipment.OTHER -> R.string.equipment_other
    }

@get:StringRes
val ExerciseType.labelRes: Int
    get() = when (this) {
        ExerciseType.COMPOUND -> R.string.exercise_type_compound
        ExerciseType.ISOLATION -> R.string.exercise_type_isolation
        ExerciseType.ISOMETRIC -> R.string.exercise_type_isometric
    }

@get:StringRes
val SetType.labelRes: Int
    get() = when (this) {
        SetType.NORMAL -> R.string.set_type_normal
        SetType.WARM_UP -> R.string.set_type_warm_up
        SetType.FAILURE -> R.string.set_type_failure
        SetType.DROP_SET -> R.string.set_type_drop_set
    }

@Composable
fun MuscleGroup.label(): String = stringResource(labelRes)

@Composable
fun Equipment.label(): String = stringResource(labelRes)

@Composable
fun ExerciseType.label(): String = stringResource(labelRes)

@Composable
fun SetType.label(): String = stringResource(labelRes)
