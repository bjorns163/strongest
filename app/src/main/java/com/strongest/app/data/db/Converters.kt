package com.strongest.app.data.db

import androidx.room.TypeConverter
import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.ExerciseClassification
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.SetType

class Converters {
    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)

    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toMuscleGroupList(value: String): List<MuscleGroup> =
        if (value.isBlank()) emptyList()
        else value.split(",").mapNotNull { runCatching { MuscleGroup.valueOf(it.trim()) }.getOrNull() }

    @TypeConverter
    fun fromEquipment(value: Equipment): String = value.name

    @TypeConverter
    fun toEquipment(value: String): Equipment = Equipment.valueOf(value)

    @TypeConverter
    fun fromSetType(value: SetType): String = value.name

    @TypeConverter
    fun toSetType(value: String): SetType = SetType.valueOf(value)

    @TypeConverter
    fun fromBodyMetric(value: BodyMetric): String = value.name

    @TypeConverter
    fun toBodyMetric(value: String): BodyMetric = BodyMetric.valueOf(value)

    @TypeConverter
    fun fromExerciseClassification(value: ExerciseClassification): String = value.name

    @TypeConverter
    fun toExerciseClassification(value: String): ExerciseClassification =
        try { ExerciseClassification.valueOf(value) } catch (e: Exception) { ExerciseClassification.ISOLATION }
}
