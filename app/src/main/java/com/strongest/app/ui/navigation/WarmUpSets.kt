package com.strongest.app.ui.navigation

import java.io.Serializable

data class WarmUpSetSpec(
    val weightKg: Float,
    val reps: Int
) : Serializable

data class AddWarmUpSetsRequest(
    val workoutExerciseId: Long? = null,
    val routineExerciseId: Long? = null,
    val sets: List<WarmUpSetSpec> = emptyList()
) : Serializable
