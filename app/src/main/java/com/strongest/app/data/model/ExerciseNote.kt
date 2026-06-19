package com.strongest.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_notes")
data class ExerciseNote(
    @PrimaryKey
    val exerciseId: Long,
    val noteText: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
