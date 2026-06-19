package com.strongest.app.ui.measurements

import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.MeasurementUnit
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.weightUnitLabel

fun metricUnitLabel(metric: BodyMetric, weightUnit: WeightUnit): String =
    when (metric.unit) {
        MeasurementUnit.KG -> weightUnitLabel(weightUnit)
        MeasurementUnit.PERCENT -> "%"
        MeasurementUnit.KCAL -> "kcal"
        MeasurementUnit.CM -> "cm"
    }

fun convertCanonicalToDisplay(metric: BodyMetric, canonical: Float, weightUnit: WeightUnit): Float =
    when (metric.unit) {
        MeasurementUnit.KG -> kgToDisplay(canonical, weightUnit)
        else -> canonical
    }

fun convertDisplayToCanonical(metric: BodyMetric, display: Float, weightUnit: WeightUnit): Float =
    when (metric.unit) {
        MeasurementUnit.KG -> if (weightUnit == WeightUnit.LBS) display / 2.20462f else display
        else -> display
    }

fun formatMetricValue(metric: BodyMetric, canonical: Float, weightUnit: WeightUnit): String {
    val display = convertCanonicalToDisplay(metric, canonical, weightUnit)
    val unit = metricUnitLabel(metric, weightUnit)
    val number = when (metric.unit) {
        MeasurementUnit.KCAL -> display.toInt().toString()
        else -> if (display % 1f == 0f) display.toInt().toString() else String.format("%.1f", display)
    }
    return "$number $unit"
}
