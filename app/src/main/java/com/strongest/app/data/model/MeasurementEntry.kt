package com.strongest.app.data.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.strongest.app.R

enum class MeasurementUnit { KG, PERCENT, KCAL, CM }

enum class MetricCategory(@StringRes val displayNameRes: Int) {
    BODY(R.string.metric_category_body),
    UPPER_BODY(R.string.metric_category_upper_body),
    CORE(R.string.metric_category_core),
    LOWER_BODY(R.string.metric_category_lower_body)
}

enum class BodyMetric(
    @StringRes val displayNameRes: Int,
    val unit: MeasurementUnit,
    val category: MetricCategory,
    @StringRes val instructionsRes: Int
) {
    WEIGHT(
        R.string.metric_weight,
        MeasurementUnit.KG,
        MetricCategory.BODY,
        R.string.metric_weight_instructions
    ),
    BODY_FAT(
        R.string.metric_body_fat,
        MeasurementUnit.PERCENT,
        MetricCategory.BODY,
        R.string.metric_body_fat_instructions
    ),
    CALORIC_INTAKE(
        R.string.metric_caloric_intake,
        MeasurementUnit.KCAL,
        MetricCategory.BODY,
        R.string.metric_caloric_intake_instructions
    ),
    NECK(
        R.string.metric_neck,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_neck_instructions
    ),
    SHOULDERS(
        R.string.metric_shoulders,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_shoulders_instructions
    ),
    CHEST(
        R.string.metric_chest,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_chest_instructions
    ),
    LEFT_BICEP(
        R.string.metric_left_bicep,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_left_bicep_instructions
    ),
    RIGHT_BICEP(
        R.string.metric_right_bicep,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_right_bicep_instructions
    ),
    LEFT_FOREARM(
        R.string.metric_left_forearm,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_left_forearm_instructions
    ),
    RIGHT_FOREARM(
        R.string.metric_right_forearm,
        MeasurementUnit.CM,
        MetricCategory.UPPER_BODY,
        R.string.metric_right_forearm_instructions
    ),
    UPPER_ABS(
        R.string.metric_upper_abs,
        MeasurementUnit.CM,
        MetricCategory.CORE,
        R.string.metric_upper_abs_instructions
    ),
    WAIST(
        R.string.metric_waist,
        MeasurementUnit.CM,
        MetricCategory.CORE,
        R.string.metric_waist_instructions
    ),
    LOWER_ABS(
        R.string.metric_lower_abs,
        MeasurementUnit.CM,
        MetricCategory.CORE,
        R.string.metric_lower_abs_instructions
    ),
    HIPS(
        R.string.metric_hips,
        MeasurementUnit.CM,
        MetricCategory.CORE,
        R.string.metric_hips_instructions
    ),
    LEFT_THIGH(
        R.string.metric_left_thigh,
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        R.string.metric_left_thigh_instructions
    ),
    RIGHT_THIGH(
        R.string.metric_right_thigh,
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        R.string.metric_right_thigh_instructions
    ),
    LEFT_CALF(
        R.string.metric_left_calf,
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        R.string.metric_left_calf_instructions
    ),
    RIGHT_CALF(
        R.string.metric_right_calf,
        MeasurementUnit.CM,
        MetricCategory.LOWER_BODY,
        R.string.metric_right_calf_instructions
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
