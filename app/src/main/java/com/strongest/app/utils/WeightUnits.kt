package com.strongest.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.strongest.app.data.repository.SettingsRepository
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.data.repository.WeightUnit
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.math.roundToInt

private const val KG_PER_LB = 0.45359237f

fun kgToDisplay(kg: Float, unit: WeightUnit): Float =
    if (unit == WeightUnit.LBS) kg / KG_PER_LB else kg

fun displayToKg(value: Float, unit: WeightUnit): Float =
    if (unit == WeightUnit.LBS) value * KG_PER_LB else value

fun weightUnitLabel(unit: WeightUnit): String = when (unit) {
    WeightUnit.KG -> "kg"
    WeightUnit.LBS -> "lbs"
}

fun formatWeightForDisplay(kg: Float, unit: WeightUnit): String {
    val v = kgToDisplay(kg, unit)
    val rounded = (v * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else String.format("%.1f", rounded)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsEntryPoint {
    fun settingsRepository(): SettingsRepository
}

/** Lets dialogs that are not backed by a ViewModel reach per-exercise preferences. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkoutRepositoryEntryPoint {
    fun workoutRepository(): WorkoutRepository
}

@Composable
fun rememberWeightUnit(): State<WeightUnit> {
    val context = LocalContext.current
    val repo = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java)
            .settingsRepository()
    }
    return repo.weightUnitFlow.collectAsState(initial = WeightUnit.KG)
}
