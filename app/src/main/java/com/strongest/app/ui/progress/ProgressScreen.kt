package com.strongest.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.strongest.app.data.db.MuscleVolume
import com.strongest.app.data.db.PersonalRecord
import com.strongest.app.data.db.VolumeByDate
import com.strongest.app.data.db.WorkoutsPerDay
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.ui.exercise.FILTERABLE_EQUIPMENT
import com.strongest.app.ui.exercise.FILTERABLE_MUSCLE_GROUPS
import com.strongest.app.utils.DAY_MS
import com.strongest.app.utils.dailyEntries
import com.strongest.app.utils.daySlotCount
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.localDayStart
import com.strongest.app.utils.weightUnitLabel
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ProgressScreen(
    onMeasurementsClick: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val bodyFigure by viewModel.bodyFigure.collectAsState()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                TextButton(
                    onClick = onMeasurementsClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("View Body Measurements →")
                }
            }

            item {
                Column {
                    RangePicker(
                        current = state.range,
                        onSelect = { viewModel.setRange(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    MetricPicker(
                        current = state.metric,
                        onSelect = { viewModel.setMetric(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    // Carries the focused muscle's exact value, which the radar and the
                    // heatmap cannot show themselves.
                    val focused = state.selectedMuscle
                    val focusedValue = focused?.let { mg ->
                        state.muscleVolume.firstOrNull { it.muscleGroup == mg.name }
                            ?.let { formatMetricValue(metricValue(it, state.metric, weightUnit), state.metric, weightUnit) }
                            ?: "nothing in range"
                    }
                    Text(
                        text = if (focused == null) "Focus a muscle"
                        else "${muscleLabel(focused.name)}  ·  $focusedValue — tap again to clear",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (focused == null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FILTERABLE_MUSCLE_GROUPS.size) { idx ->
                            val mg = FILTERABLE_MUSCLE_GROUPS[idx]
                            FilterChip(
                                selected = state.selectedMuscle == mg,
                                onClick = { viewModel.selectMuscle(mg) },
                                label = { Text(muscleLabel(mg.name)) }
                            )
                        }
                    }
                }
            }

            item { SectionHeader(perDayTitle(state.metric)) }
            item {
                PerWorkoutChartCard(
                    metric = state.metric,
                    weightUnit = weightUnit,
                    rangeDays = state.range.days,
                    volumeByDay = state.volumeByDay,
                    perDay = state.workoutsPerDay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader("Recovery") }
            item {
                RecoveryCard(
                    recovering = state.recoveringMuscles,
                    selected = state.selectedMuscle,
                    onSelect = { viewModel.selectMuscle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader(muscleTitle(state.metric)) }
            item {
                MuscleChartCard(
                    metric = state.metric,
                    weightUnit = weightUnit,
                    muscle = state.muscleVolume,
                    selected = state.selectedMuscle,
                    onSelect = { viewModel.selectMuscle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader("Muscle Balance") }
            item {
                MuscleRadarCard(
                    metric = state.metric,
                    weightUnit = weightUnit,
                    muscle = state.muscleVolume,
                    selected = state.selectedMuscle,
                    onSelect = { viewModel.selectMuscle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader("Muscle Heatmap") }
            item {
                BodyHeatmapCard(
                    metric = state.metric,
                    muscle = state.muscleVolume,
                    weightUnit = weightUnit,
                    figure = bodyFigure,
                    selected = state.selectedMuscle,
                    onSelect = { viewModel.selectMuscle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader("Personal Records") }
            item {
                Text(
                    text = "Equipment",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.prEquipmentFilter == null,
                            onClick = { viewModel.setPrEquipmentFilter(null) },
                            label = { Text("All") }
                        )
                    }
                    items(FILTERABLE_EQUIPMENT.size) { idx ->
                        val eq = FILTERABLE_EQUIPMENT[idx]
                        FilterChip(
                            selected = state.prEquipmentFilter == eq,
                            onClick = { viewModel.setPrEquipmentFilter(eq) },
                            label = { Text(eq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            val filteredPrs = state.personalRecords.filter { pr ->
                val muscleMatch = state.selectedMuscle?.let { it.name == pr.muscleGroup } ?: true
                val equipMatch = state.prEquipmentFilter?.let { it.name == pr.equipment } ?: true
                muscleMatch && equipMatch
            }
            if (filteredPrs.isEmpty()) {
                item {
                    Text(
                        text = if (state.personalRecords.isEmpty()) {
                            "Complete workouts to see your personal records"
                        } else {
                            "No records match the selected filters"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredPrs.size) { index ->
                    PersonalRecordCard(filteredPrs[index], weightUnit)
                }
            }
        }
    }
}

private fun perDayTitle(metric: ProgressMetric): String = when (metric) {
    ProgressMetric.WEIGHT -> "Volume per Day"
    ProgressMetric.SETS -> "Sets per Day"
    ProgressMetric.WORKOUTS -> "Workouts per Day"
}

private fun muscleTitle(metric: ProgressMetric): String = when (metric) {
    ProgressMetric.WEIGHT -> "Volume by Muscle Group"
    ProgressMetric.SETS -> "Sets by Muscle Group"
    ProgressMetric.WORKOUTS -> "Workouts by Muscle Group"
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun RecoveryCard(
    recovering: List<MuscleRecovery>,
    selected: MuscleGroup?,
    onSelect: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recovering muscles",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (recovering.isEmpty()) {
                Text(
                    text = "All trained muscles are recovered — ready to train.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                recovering.forEach { item ->
                    val name = muscleLabel(item.muscleGroup.name)
                    val isFocused = selected == item.muscleGroup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(item.muscleGroup) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Text(
                            text = "${item.hoursRemaining}h left",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.fractionRecovered },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun RangePicker(
    current: ProgressRange,
    onSelect: (ProgressRange) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ProgressRange.entries.size) { idx ->
            val range = ProgressRange.entries[idx]
            FilterChip(
                selected = current == range,
                onClick = { onSelect(range) },
                label = { Text(range.label) }
            )
        }
    }
}

@Composable
private fun MetricPicker(
    current: ProgressMetric,
    onSelect: (ProgressMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProgressMetric.entries.forEach { metric ->
            FilterChip(
                selected = current == metric,
                onClick = { onSelect(metric) },
                label = { Text(metric.label) }
            )
        }
    }
}

@Composable
private fun PerWorkoutChartCard(
    metric: ProgressMetric,
    weightUnit: WeightUnit,
    rangeDays: Int,
    volumeByDay: List<VolumeByDate>,
    perDay: List<WorkoutsPerDay>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        val dateFormat = SimpleDateFormat("MMM d", LocalConfiguration.current.locales[0])
        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val markerBg = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
        val markerStroke = MaterialTheme.colorScheme.outline.toArgb()

        // The x-axis spans every calendar day in the selected range; days without data simply
        // have no point, and the line connects across them.
        val lastDay = localDayStart(System.currentTimeMillis())
        val startDay = lastDay - (rangeDays - 1) * DAY_MS
        val volumeMap = volumeByDay.associateBy { it.date }
        val perDayMap = perDay.associateBy { it.dayStart }
        val points = dailyEntries(startDay, lastDay) { day ->
            when (metric) {
                ProgressMetric.WEIGHT -> volumeMap[day]?.let { kgToDisplay(it.totalVolumeKg, weightUnit) }
                ProgressMetric.SETS -> volumeMap[day]?.let { it.totalSets.toFloat() }
                ProgressMetric.WORKOUTS -> perDayMap[day]?.let { it.count.toFloat() }
            }
        }
        if (points.isEmpty()) {
            ChartEmpty()
            return@Card
        }
        val slotCount = daySlotCount(startDay, lastDay)
        val dsLabel = when (metric) {
            ProgressMetric.WEIGHT -> "Volume (${weightUnitLabel(weightUnit)})"
            ProgressMetric.SETS -> "Sets"
            ProgressMetric.WORKOUTS -> "Workouts"
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(8.dp),
            factory = { ctx ->
                LineChart(ctx).apply {
                    description.isEnabled = false
                    legend.textColor = onSurface
                    axisRight.isEnabled = false
                    axisLeft.textColor = onSurface
                    axisLeft.setDrawGridLines(false)
                    axisLeft.axisMinimum = 0f
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = onSurface
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    setNoDataTextColor(onSurface)
                    setTouchEnabled(true)
                    setScaleEnabled(true)
                    isHighlightPerTapEnabled = true
                    isHighlightPerDragEnabled = false
                }
            },
            update = { chart ->
                val dataSet = LineDataSet(points, dsLabel).apply {
                    color = primary
                    valueTextColor = onSurface
                    circleColors = listOf(primary)
                    setDrawValues(false)
                    lineWidth = 2f
                    circleRadius = 3f
                }
                chart.data = LineData(dataSet)
                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val idx = value.toInt()
                        if (idx < 0 || idx >= slotCount) return ""
                        return dateFormat.format(Date(localDayStart(startDay + idx * DAY_MS)))
                    }
                }
                chart.xAxis.axisMinimum = 0f
                chart.xAxis.axisMaximum = (slotCount - 1).coerceAtLeast(1).toFloat()
                chart.xAxis.labelCount = 6
                // Tap a point to read its exact value.
                chart.marker = ChartMarkerView(chart.context, onSurface, markerBg, markerStroke) { entry, _ ->
                    val day = localDayStart(startDay + entry.x.toLong() * DAY_MS)
                    "${dateFormat.format(Date(day))}  ·  ${formatMetricValue(entry.y, metric, weightUnit)}"
                }.apply { chartView = chart }
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

@Composable
private fun MuscleChartCard(
    metric: ProgressMetric,
    weightUnit: WeightUnit,
    muscle: List<MuscleVolume>,
    selected: MuscleGroup?,
    onSelect: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        if (muscle.isEmpty()) {
            ChartEmpty()
            return@Card
        }

        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val muted = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f).toArgb()
        val markerBg = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
        val markerStroke = MaterialTheme.colorScheme.outline.toArgb()

        val sorted = muscle.sortedByDescending { metricValue(it, metric, weightUnit) }
        val labels = sorted.map { muscleLabel(it.muscleGroup) }
        val barColors = sorted.map { mv ->
            if (selected == null || selected.name == mv.muscleGroup) primary else muted
        }
        val entries = sorted.mapIndexed { idx, mv ->
            BarEntry(idx.toFloat(), metricValue(mv, metric, weightUnit))
        }
        val dsLabel = when (metric) {
            ProgressMetric.WEIGHT -> "Volume (${weightUnitLabel(weightUnit)})"
            ProgressMetric.SETS -> "Sets"
            ProgressMetric.WORKOUTS -> "Workouts"
        }
        val chartHeight = (60 + sorted.size * 28).coerceIn(180, 480).dp

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(8.dp),
            factory = { ctx ->
                HorizontalBarChart(ctx).apply {
                    description.isEnabled = false
                    legend.textColor = onSurface
                    axisRight.isEnabled = false
                    axisLeft.textColor = onSurface
                    axisLeft.setDrawGridLines(false)
                    axisLeft.axisMinimum = 0f
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = onSurface
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    setNoDataTextColor(onSurface)
                    setFitBars(true)
                    isHighlightPerTapEnabled = true
                    // Off, or scrolling the page over the chart selects whatever bar
                    // the finger passes.
                    isHighlightPerDragEnabled = false
                }
            },
            update = { chart ->
                val dataSet = BarDataSet(entries, dsLabel).apply {
                    colors = barColors
                    // The bar itself shows the focus; the tooltip carries the value.
                    highLightAlpha = 0
                    valueTextColor = onSurface
                    valueFormatter = object : ValueFormatter() {
                        // Sets can be fractional, because a secondary muscle counts as
                        // part of a set; truncating here disagreed with the tooltip.
                        override fun getFormattedValue(value: Float): String = when {
                            value >= 1000 -> String.format(Locale.getDefault(), "%.1fk", value / 1000)
                            value % 1f == 0f -> value.toInt().toString()
                            else -> String.format(Locale.getDefault(), "%.1f", value)
                        }
                    }
                }
                chart.data = BarData(dataSet).apply { barWidth = 0.6f }
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                chart.xAxis.labelCount = labels.size
                // One swatch, like the other charts: the per-bar colours are a
                // highlight, and left alone the legend prints an entry per colour.
                chart.legend.setCustom(
                    listOf(
                        LegendEntry(dsLabel, Legend.LegendForm.SQUARE, Float.NaN, Float.NaN, null, primary)
                    )
                )
                chart.marker = ChartMarkerView(chart.context, onSurface, markerBg, markerStroke) { entry, _ ->
                    val name = labels.getOrNull(entry.x.toInt()).orEmpty()
                    "$name  ·  ${formatMetricValue(entry.y, metric, weightUnit)}"
                }.apply { chartView = chart }
                // Tapping a bar focuses that muscle across the whole tab.
                chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val idx = e?.x?.toInt() ?: return
                        sorted.getOrNull(idx)?.let { mv ->
                            runCatching { MuscleGroup.valueOf(mv.muscleGroup) }.getOrNull()?.let(onSelect)
                        }
                    }

                    override fun onNothingSelected() = Unit
                })
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

@Composable
private fun MuscleRadarCard(
    metric: ProgressMetric,
    weightUnit: WeightUnit,
    muscle: List<MuscleVolume>,
    selected: MuscleGroup?,
    onSelect: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        if (muscle.size < 3) {
            // A spiderweb needs at least 3 axes to be meaningful.
            ChartEmpty()
            return@Card
        }

        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val markerBg = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
        val markerStroke = MaterialTheme.colorScheme.outline.toArgb()

        // Sort by muscle name for a stable web shape across refreshes.
        val sorted = muscle.sortedBy { it.muscleGroup }
        val labels = sorted.map { muscleLabel(it.muscleGroup) }
        val selectedIndex = sorted.indexOfFirst { selected != null && it.muscleGroup == selected.name }
        val entries = sorted.map { RadarEntry(metricValue(it, metric, weightUnit)) }
        val dsLabel = when (metric) {
            ProgressMetric.WEIGHT -> "Volume (${weightUnitLabel(weightUnit)})"
            ProgressMetric.SETS -> "Sets"
            ProgressMetric.WORKOUTS -> "Workouts"
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(8.dp),
            factory = { ctx ->
                RadarChart(ctx).apply {
                    description.isEnabled = false
                    legend.textColor = onSurface
                    webColor = onSurface
                    webColorInner = onSurface
                    webLineWidth = 1f
                    webLineWidthInner = 1f
                    webAlpha = 120
                    setNoDataTextColor(onSurface)
                    yAxis.setDrawLabels(false)
                    yAxis.axisMinimum = 0f
                    // Tap a spoke to read its value and focus that muscle; drag to turn
                    // the web. Turning it does mean a drag started inside the web rotates
                    // rather than scrolling the page.
                    isHighlightPerTapEnabled = true
                    isRotationEnabled = true
                    xAxis.textColor = onSurface
                    xAxis.textSize = 10f
                }
            },
            update = { chart ->
                val dataSet = RadarDataSet(entries, dsLabel).apply {
                    color = primary
                    fillColor = primary
                    setDrawFilled(true)
                    fillAlpha = 90
                    lineWidth = 2f
                    setDrawValues(false)
                    setDrawHighlightCircleEnabled(true)
                    setDrawHighlightIndicators(true)
                    highlightCircleFillColor = primary
                    highlightCircleStrokeColor = onSurface
                    highlightCircleStrokeWidth = 2f
                    highlightCircleInnerRadius = 0f
                    highlightCircleOuterRadius = 6f
                    highLightColor = primary
                }
                chart.data = RadarData(dataSet)
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                chart.marker = ChartMarkerView(chart.context, onSurface, markerBg, markerStroke) { entry, highlight ->
                    val name = labels.getOrNull(highlight?.x?.toInt() ?: -1).orEmpty()
                    "$name  ·  ${formatMetricValue(entry.y, metric, weightUnit)}"
                }.apply { chartView = chart }
                chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val idx = h?.x?.toInt() ?: return
                        sorted.getOrNull(idx)?.let { mv ->
                            runCatching { MuscleGroup.valueOf(mv.muscleGroup) }.getOrNull()?.let(onSelect)
                        }
                    }

                    override fun onNothingSelected() = Unit
                })
                chart.notifyDataSetChanged()
                // Mark the focused muscle's spoke.
                if (selectedIndex >= 0) {
                    chart.highlightValues(arrayOf(Highlight(selectedIndex.toFloat(), 0, 0)))
                } else {
                    chart.highlightValues(null)
                }
                chart.invalidate()
            }
        )
    }
}

/** The exact value, with its unit, for a tooltip or caption. */
private fun formatMetricValue(value: Float, metric: ProgressMetric, weightUnit: WeightUnit): String {
    val rounded = if (value % 1f == 0f) value.toInt().toString()
    else String.format(Locale.getDefault(), "%.1f", value)
    return when (metric) {
        ProgressMetric.WEIGHT -> "$rounded ${weightUnitLabel(weightUnit)}"
        ProgressMetric.SETS -> if (value == 1f) "1 set" else "$rounded sets"
        ProgressMetric.WORKOUTS -> if (value == 1f) "1 workout" else "$rounded workouts"
    }
}

/** "LOWER_BACK" -> "Lower back". */
private fun muscleLabel(raw: String): String =
    raw.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")

private fun metricValue(mv: MuscleVolume, metric: ProgressMetric, weightUnit: WeightUnit): Float =
    when (metric) {
        ProgressMetric.WEIGHT -> kgToDisplay(mv.totalVolumeKg, weightUnit)
        ProgressMetric.SETS -> mv.totalSets
        ProgressMetric.WORKOUTS -> mv.workoutCount.toFloat()
    }

@Composable
private fun BodyHeatmapCard(
    metric: ProgressMetric,
    muscle: List<MuscleVolume>,
    weightUnit: WeightUnit,
    figure: BodyFigure,
    selected: MuscleGroup?,
    onSelect: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        if (muscle.isEmpty()) {
            ChartEmpty()
            return@Card
        }
        val values: Map<MuscleGroup, Float> = muscle
            .mapNotNull { mv ->
                runCatching { MuscleGroup.valueOf(mv.muscleGroup) }.getOrNull()
                    ?.let { it to metricValue(mv, metric, weightUnit) }
            }
            .toMap()
        BodyHeatmap(
            muscleValues = values,
            figure = figure,
            selected = selected,
            onSelect = onSelect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
private fun ChartEmpty() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data in this range",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersonalRecordCard(
    pr: PersonalRecord,
    weightUnit: WeightUnit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", LocalConfiguration.current.locales[0])
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pr.exerciseName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = dateFormat.format(Date(pr.workoutDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (pr.muscleGroup == "CARDIO") {
                    "${formatWeightForDisplay(pr.maxWeightKg, weightUnit)} × ${pr.maxReps}"
                } else {
                    "${formatWeightForDisplay(pr.maxWeightKg, weightUnit)} ${weightUnitLabel(weightUnit)} × ${pr.maxReps}"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
