package com.strongest.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MeasurementUnit { KG, PERCENT, KCAL, CM }

enum class MetricCategory(val displayName: String) {
    BODY("Body"),
    UPPER_BODY("Upper Body"),
    CORE("Core"),
    LOWER_BODY("Lower Body")
}

enum class BodyMetric(
    val displayName: String,
    val unit: MeasurementUnit,
    val category: MetricCategory,
    val instructions: String
) {
    WEIGHT(
        "Weight",
        MeasurementUnit.KG,
        MetricCategory.BODY,
        "Weigh yourself first thing in the morning, after using the bathroom and before eating or drinking. Wear minimal clothing for consistency."
    ),
    BODY_FAT(
        "Body Fat",
        MeasurementUnit.PERCENT,
        MetricCategory.BODY,
        "Use the same method every time: smart scale, calipers, or DEXA. Measure under consistent conditions (e.g. fasted, morning)."
    ),
    CALORIC_INTAKE(
        "Caloric Intake",
        MeasurementUnit.KCAL,
        MetricCategory.BODY,
        "Log the total calories you consumed over the day. Track at the same time each day for consistent comparisons."
    ),
    NECK(
        "Neck",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Stand relaxed with your head straight. Measure around the middle of the neck, just below the Adam's apple. Tape should be snug, not tight."
    ),
    SHOULDERS(
        "Shoulders",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Stand relaxed with arms at your sides. Wrap the tape around the widest part of your shoulders / deltoids, keeping the tape level."
    ),
    CHEST(
        "Chest",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Stand relaxed and breathe normally. Wrap the tape around the chest at nipple level, keeping it horizontal across the back."
    ),
    LEFT_BICEP(
        "Left Bicep",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Flex your left arm at 90°. Measure around the largest part of the biceps, keeping the tape perpendicular to the arm."
    ),
    RIGHT_BICEP(
        "Right Bicep",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Flex your right arm at 90°. Measure around the largest part of the biceps, keeping the tape perpendicular to the arm."
    ),
    LEFT_FOREARM(
        "Left Forearm",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Let your left arm hang at your side, palm facing forward. Measure around the widest point of the forearm below the elbow."
    ),
    RIGHT_FOREARM(
        "Right Forearm",
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        "Let your right arm hang at your side, palm facing forward. Measure around the widest point of the forearm below the elbow."
    ),
    UPPER_ABS(
        "Upper Abs",
        MeasurementUnit.CM,
        MetricCategory.CORE,
        "Stand relaxed and exhale normally. Wrap the tape just below the chest, around the upper abdomen. Keep the tape level."
    ),
    WAIST(
        "Waist",
        MeasurementUnit.CM,
        MetricCategory.CORE,
        "Stand relaxed and exhale normally. Measure around the narrowest part of the waist, usually around the navel. Do not suck in."
    ),
    LOWER_ABS(
        "Lower Abs",
        MeasurementUnit.CM,
        MetricCategory.CORE,
        "Stand relaxed and exhale normally. Measure around the lower abdomen, roughly two finger-widths below the navel."
    ),
    HIPS(
        "Hips",
        MeasurementUnit.CM,
        MetricCategory.CORE,
        "Stand with feet together. Measure around the widest part of the hips / glutes. Keep the tape level all the way around."
    ),
    LEFT_THIGH(
        "Left Thigh",
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        "Stand with weight evenly distributed. Measure around the largest part of the left thigh, just below the glute."
    ),
    RIGHT_THIGH(
        "Right Thigh",
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        "Stand with weight evenly distributed. Measure around the largest part of the right thigh, just below the glute."
    ),
    LEFT_CALF(
        "Left Calf",
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        "Stand relaxed. Measure around the widest part of the left calf, keeping the tape perpendicular to the leg."
    ),
    RIGHT_CALF(
        "Right Calf",
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        "Stand relaxed. Measure around the widest part of the right calf, keeping the tape perpendicular to the leg."
    )
}

@Entity(
    tableName = "measurement_entries",
    indices = [Index("metric"), Index("timestamp")]
)
data class MeasurementEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val metric: BodyMetric,
    val value: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
