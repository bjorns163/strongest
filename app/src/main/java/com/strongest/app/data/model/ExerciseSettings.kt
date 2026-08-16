package com.strongest.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_settings")
data class ExerciseSettings(
    @PrimaryKey
    val exerciseId: Long,
    val warmUpSetCount: Int = 3
)
