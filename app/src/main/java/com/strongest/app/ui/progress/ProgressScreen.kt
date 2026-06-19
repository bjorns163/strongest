package com.strongest.app.ui.progress

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
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
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.weightUnitLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    onMeasurementsClick: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()

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
                }
            }

            item { SectionHeader(perWorkoutTitle(state.metric)) }
            item {
                PerWorkoutChartCard(
                    metric = state.metric,
                    weightUnit = weightUnit,
                    volume = state.volumeByDate,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader("Personal Records") }
            item {
                Text(
                    text = "Muscle Group",
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
                            selected = state.prMuscleFilter == null,
                            onClick = { viewModel.setPrMuscleFilter(null) },
                            label = { Text("All") }
                        )
                    }
                    items(FILTERABLE_MUSCLE_GROUPS.size) { idx ->
                        val mg = FILTERABLE_MUSCLE_GROUPS[idx]
                        FilterChip(
                            selected = state.prMuscleFilter == mg,
                            onClick = { viewModel.setPrMuscleFilter(mg) },
                            label = { Text(mg.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
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
                val muscleMatch = state.prMuscleFilter?.let { it.name == pr.muscleGroup } ?: true
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

private fun perWorkoutTitle(metric: ProgressMetric): String = when (metric) {
    ProgressMetric.WEIGHT -> "Volume per Workout"
    ProgressMetric.SETS -> "Sets per Workout"
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
                    val name = item.muscleGroup.name.lowercase()
                        .replaceFirstChar { it.uppercase() }
                        .replace("_", " ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyMedium)
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProgressRange.entries.forEach { range ->
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
    volume: List<VolumeByDate>,
    perDay: List<WorkoutsPerDay>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()

        val (labels, values, dsLabel) = when (metric) {
            ProgressMetric.WEIGHT -> {
                val sorted = volume.sortedBy { it.date }
                Triple(
                    sorted.map { dateFormat.format(Date(it.date)) },
                    sorted.map { kgToDisplay(it.totalVolumeKg, weightUnit) },
                    "Volume (${weightUnitLabel(weightUnit)})"
                )
            }
            ProgressMetric.SETS -> {
                val sorted = volume.sortedBy { it.date }
                Triple(
                    sorted.map { dateFormat.format(Date(it.date)) },
                    sorted.map { it.totalSets.toFloat() },
                    "Sets"
                )
            }
            ProgressMetric.WORKOUTS -> {
                val sorted = perDay.sortedBy { it.dayStart }
                Triple(
                    sorted.map { dateFormat.format(Date(it.dayStart)) },
                    sorted.map { it.count.toFloat() },
                    "Workouts"
                )
            }
        }

        if (values.isEmpty()) {
            ChartEmpty()
        } else {
            val entries = values.mapIndexed { idx, v -> Entry(idx.toFloat(), v) }
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
                    }
                },
                update = { chart ->
                    val dataSet = LineDataSet(entries, dsLabel).apply {
                        color = primary
                        valueTextColor = onSurface
                        circleColors = listOf(primary)
                        setDrawValues(false)
                        lineWidth = 2f
                        circleRadius = 3f
                    }
                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.xAxis.labelCount = minOf(labels.size, 6)
                    chart.notifyDataSetChanged()
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun MuscleChartCard(
    metric: ProgressMetric,
    weightUnit: WeightUnit,
    muscle: List<MuscleVolume>,
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

        val sorted = muscle.sortedByDescending { metricValue(it, metric, weightUnit) }
        val labels = sorted.map { it.muscleGroup.lowercase().replaceFirstChar { c -> c.uppercase() } }
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
                }
            },
            update = { chart ->
                val dataSet = BarDataSet(entries, dsLabel).apply {
                    color = primary
                    valueTextColor = onSurface
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value >= 1000) String.format("%.1fk", value / 1000) else value.toInt().toString()
                        }
                    }
                }
                chart.data = BarData(dataSet).apply { barWidth = 0.6f }
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                chart.xAxis.labelCount = labels.size
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

        // Sort by muscle name for a stable web shape across refreshes.
        val sorted = muscle.sortedBy { it.muscleGroup }
        val labels = sorted.map {
            it.muscleGroup.lowercase().replaceFirstChar { c -> c.uppercase() }.replace("_", " ")
        }
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
                }
                chart.data = RadarData(dataSet)
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }
}

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
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
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
                text = "${formatWeightForDisplay(pr.maxWeightKg, weightUnit)} ${weightUnitLabel(weightUnit)} × ${pr.maxReps}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
