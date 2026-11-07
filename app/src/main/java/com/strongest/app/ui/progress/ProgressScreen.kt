package com.strongest.app.ui.progress

import android.content.res.Resources
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
import com.strongest.app.R
import com.strongest.app.data.db.CardioSummary
import com.strongest.app.data.db.MuscleVolume
import com.strongest.app.data.db.PersonalRecord
import com.strongest.app.data.db.VolumeByDate
import com.strongest.app.data.db.WorkoutsPerDay
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.SECONDARY_MUSCLE_WEIGHT
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.ui.components.label
import com.strongest.app.ui.exercise.FILTERABLE_EQUIPMENT
import com.strongest.app.ui.exercise.FILTERABLE_MUSCLE_GROUPS
import com.strongest.app.utils.addDays
import com.strongest.app.utils.dailyEntries
import com.strongest.app.utils.daySlotCount
import com.strongest.app.utils.formatDuration
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.localDayStart
import com.strongest.app.utils.rememberLocalizedDateFormat
import com.strongest.app.utils.weightUnitLabel
import java.util.Locale
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    onMeasurementsClick: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val bodyFigure by viewModel.bodyFigure.collectAsState()

    // The tab's data is loaded on demand, not observed, and this ViewModel survives tab switches.
    // Without this, recovery and personal records keep the values they had when the tab was first
    // opened — a workout finished afterwards only showed up once the app was restarted. (The
    // charts appeared to update because touching the range selector reloads them via loadRanged.)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = stringResource(R.string.nav_progress),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                TextButton(
                    onClick = onMeasurementsClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(stringResource(R.string.view_body_measurements))
                }
            }

            item {
                Column {
                    RangePicker(
                        current = state.range,
                        startDay = state.startDay,
                        endDay = state.endDay,
                        firstDataDay = state.firstDataDay,
                        onSelect = { viewModel.setRange(it) },
                        onSelectCustom = { start, end -> viewModel.setCustomRange(start, end) },
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
                    val resources = LocalResources.current
                    val focusedValue = focused?.let { mg ->
                        state.muscleVolume.firstOrNull { it.muscleGroup == mg.name }
                            ?.let { formatMetricValue(resources, metricValue(it, state.metric, weightUnit), state.metric, weightUnit) }
                            ?: stringResource(R.string.nothing_in_range)
                    }
                    Text(
                        text = if (focused == null) stringResource(R.string.focus_a_muscle)
                        else stringResource(R.string.focused_muscle_value, focused.label(), focusedValue.orEmpty()),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (focused == null) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PROGRESS_MUSCLE_GROUPS.size) { idx ->
                            val mg = PROGRESS_MUSCLE_GROUPS[idx]
                            FilterChip(
                                selected = state.selectedMuscle == mg,
                                onClick = { viewModel.selectMuscle(mg) },
                                label = { Text(mg.label()) }
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
                    startDay = state.startDay,
                    lastDay = state.endDay,
                    volumeByDay = state.volumeByDay,
                    perDay = state.workoutsPerDay,
                    prsPerDay = state.prsPerDay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader(stringResource(R.string.settings_recovery)) }
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

            item {
                SectionHeader(
                    text = muscleTitle(state.metric),
                    infoTitle = muscleTitle(state.metric),
                    infoText = muscleInfo(state.metric, weightUnit)
                )
            }
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

            item { SectionHeader(stringResource(R.string.muscle_balance)) }
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

            item { SectionHeader(stringResource(R.string.muscle_heatmap)) }
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

            item { SectionHeader(stringResource(R.string.muscle_cardio)) }
            item {
                CardioCard(
                    cardio = state.cardio,
                    weightUnit = weightUnit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item { SectionHeader(stringResource(R.string.personal_records)) }
            item {
                Text(
                    text = stringResource(R.string.equipment),
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
                            label = { Text(stringResource(R.string.filter_all)) }
                        )
                    }
                    items(FILTERABLE_EQUIPMENT.size) { idx ->
                        val eq = FILTERABLE_EQUIPMENT[idx]
                        FilterChip(
                            selected = state.prEquipmentFilter == eq,
                            onClick = { viewModel.setPrEquipmentFilter(eq) },
                            label = { Text(eq.label()) }
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
                            stringResource(R.string.no_prs_yet)
                        } else {
                            stringResource(R.string.no_prs_match)
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

/** Cardio isn't a muscle on this tab — it has its own card — so it can't be focused. */
private val PROGRESS_MUSCLE_GROUPS = FILTERABLE_MUSCLE_GROUPS.filter { it != MuscleGroup.CARDIO }

@Composable
private fun perDayTitle(metric: ProgressMetric): String = stringResource(
    when (metric) {
        ProgressMetric.WEIGHT -> R.string.volume_per_day
        ProgressMetric.SETS -> R.string.sets_per_day
        ProgressMetric.WORKOUTS -> R.string.workouts_per_day
        ProgressMetric.PRS -> R.string.prs_per_day
    }
)

/** How each metric is credited to muscles — the "0.5 sets" otherwise looks like a bug. */
@Composable
private fun muscleInfo(metric: ProgressMetric, weightUnit: WeightUnit): String {
    val secondary = SECONDARY_MUSCLE_WEIGHT.toString().removeSuffix(".0")
    val unit = weightUnitLabel(weightUnit)
    val shared = stringResource(R.string.muscle_info_shared)
    return when (metric) {
        ProgressMetric.SETS ->
            stringResource(R.string.muscle_info_sets, secondary) + "\n\n" + shared
        ProgressMetric.WEIGHT ->
            stringResource(
                R.string.muscle_info_volume,
                (SECONDARY_MUSCLE_WEIGHT * 100).toInt(),
                unit,
                (1000 * SECONDARY_MUSCLE_WEIGHT).toInt()
            ) + "\n\n" + shared
        ProgressMetric.WORKOUTS ->
            stringResource(R.string.muscle_info_workouts) + "\n\n" + shared
        ProgressMetric.PRS -> stringResource(R.string.muscle_info_prs)
    }
}

@Composable
private fun muscleTitle(metric: ProgressMetric): String = stringResource(
    when (metric) {
        ProgressMetric.WEIGHT -> R.string.volume_by_muscle_group
        ProgressMetric.SETS -> R.string.sets_by_muscle_group
        ProgressMetric.WORKOUTS -> R.string.workouts_by_muscle_group
        ProgressMetric.PRS -> R.string.prs_by_muscle_group
    }
)

@Composable
private fun SectionHeader(text: String, infoTitle: String? = null, infoText: String? = null) {
    if (infoText == null) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        return
    }
    var showInfo by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { showInfo = true }) {
            Icon(Icons.Default.Info, stringResource(R.string.how_this_is_counted))
        }
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(infoTitle ?: text) },
            text = { Text(infoText) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text(stringResource(R.string.got_it)) }
            }
        )
    }
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
                text = stringResource(R.string.recovering_muscles),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (recovering.isEmpty()) {
                Text(
                    text = stringResource(R.string.all_recovered),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                recovering.forEach { item ->
                    val name = item.muscleGroup.label()
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
                            text = stringResource(R.string.hours_left, item.hoursRemaining),
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

/**
 * Cardio stays out of the sets, volume and muscle charts (its "weight" is a machine level and its
 * "reps" a time), so it gets its own summary: total time, then each exercise's share of it.
 */
@Composable
private fun CardioCard(
    cardio: List<CardioSummary>,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (cardio.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_cardio_in_range),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }
            val totalSeconds = cardio.sumOf { it.totalSeconds }
            val maxSeconds = cardio.maxOf { it.totalSeconds }.coerceAtLeast(1)
            val dateFormat = rememberLocalizedDateFormat("MMMd")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CardioStat(label = stringResource(R.string.total_time), value = formatDuration(totalSeconds))
                CardioStat(label = stringResource(R.string.sessions), value = cardio.sumOf { it.sessions }.toString())
                CardioStat(label = stringResource(R.string.exercises), value = cardio.size.toString())
            }
            Spacer(modifier = Modifier.height(16.dp))

            cardio.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.exerciseName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val sessions = pluralStringResource(R.plurals.session_count, item.sessions, item.sessions)
                        val level = if (item.maxLevel > 0f) {
                            "  ·  " + stringResource(R.string.max_level_value, formatWeightForDisplay(item.maxLevel, weightUnit))
                        } else ""
                        Text(
                            text = "$sessions$level  ·  " + stringResource(R.string.last_done, dateFormat.format(Date(item.lastDone))),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatDuration(item.totalSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.totalSeconds.toFloat() / maxSeconds },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun CardioStat(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RangePicker(
    current: ProgressRange,
    startDay: Long,
    endDay: Long,
    firstDataDay: Long?,
    onSelect: (ProgressRange) -> Unit,
    onSelectCustom: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustom by remember { mutableStateOf(false) }
    val dateFormat = rememberLocalizedDateFormat("MMMd")
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ProgressRange.entries.size) { idx ->
            val range = ProgressRange.entries[idx]
            val isCustom = range == ProgressRange.CUSTOM
            FilterChip(
                selected = current == range,
                onClick = { if (isCustom) showCustom = true else onSelect(range) },
                label = {
                    // Once picked, the custom chip names its own dates.
                    Text(
                        if (isCustom && current == range) {
                            "${dateFormat.format(Date(startDay))} – ${dateFormat.format(Date(endDay))}"
                        } else stringResource(range.labelRes)
                    )
                },
                leadingIcon = if (isCustom) {
                    { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
    if (showCustom) {
        CustomRangeDialog(
            firstDataDay = firstDataDay,
            initialStart = startDay,
            initialEnd = endDay,
            onApply = { start, end ->
                showCustom = false
                onSelectCustom(start, end)
            },
            onDismiss = { showCustom = false }
        )
    }
}

/**
 * Picks a custom range with a two-thumb slider. The slider only spans days the app has data for:
 * from the first finished workout up to today, one step per day.
 */
@Composable
private fun CustomRangeDialog(
    firstDataDay: Long?,
    initialStart: Long,
    initialEnd: Long,
    onApply: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val today = localDayStart(System.currentTimeMillis())
    val dateFormat = rememberLocalizedDateFormat("MMMdyyyy")
    val first = firstDataDay?.coerceAtMost(today)
    val lastIndex = first?.let { daySlotCount(it, today) - 1 } ?: 0
    fun dayAt(index: Int): Long = addDays(first!!, index)
    fun indexOf(day: Long): Int =
        if (first == null) 0 else (daySlotCount(first, day.coerceIn(first, today)) - 1).coerceIn(0, lastIndex)

    var selection by remember {
        mutableStateOf(indexOf(initialStart).toFloat()..indexOf(initialEnd).toFloat())
    }
    val startIdx = selection.start.roundToInt()
    val endIdx = selection.endInclusive.roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_range)) },
        text = {
            if (first == null) {
                Text(stringResource(R.string.custom_range_no_data))
            } else Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            stringResource(R.string.from),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            dateFormat.format(Date(dayAt(startIdx))),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(R.string.to),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            dateFormat.format(Date(dayAt(endIdx))),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (lastIndex > 0) {
                    RangeSlider(
                        value = selection,
                        // Snap to whole days by hand: `steps` would draw a tick per day, which
                        // turns into a solid dotted bar once there's a year or more of data.
                        onValueChange = { selection = it.start.roundToInt().toFloat()..it.endInclusive.roundToInt().toFloat() },
                        valueRange = 0f..lastIndex.toFloat()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            dateFormat.format(Date(first)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.today),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        stringResource(R.string.all_workouts_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val days = endIdx - startIdx + 1
                Text(
                    pluralStringResource(R.plurals.day_count, days, days),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(dayAt(startIdx), dayAt(endIdx)) },
                enabled = first != null
            ) { Text(stringResource(R.string.apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
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
                label = { Text(stringResource(metric.labelRes)) }
            )
        }
    }
}

@Composable
private fun PerWorkoutChartCard(
    metric: ProgressMetric,
    weightUnit: WeightUnit,
    startDay: Long,
    lastDay: Long,
    volumeByDay: List<VolumeByDate>,
    perDay: List<WorkoutsPerDay>,
    prsPerDay: List<PrsPerDay>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        val dateFormat = rememberLocalizedDateFormat("MMMd")
        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val markerBg = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
        val markerStroke = MaterialTheme.colorScheme.outline.toArgb()

        // The x-axis spans every calendar day in the selected range; days without data simply
        // have no point, and the line connects across them.
        val volumeMap = volumeByDay.associateBy { it.date }
        val perDayMap = perDay.associateBy { it.dayStart }
        val prsMap = prsPerDay.associateBy { it.dayStart }
        val points = dailyEntries(startDay, lastDay) { day ->
            when (metric) {
                ProgressMetric.WEIGHT -> volumeMap[day]?.let { kgToDisplay(it.totalVolumeKg, weightUnit) }
                ProgressMetric.SETS -> volumeMap[day]?.let { it.totalSets.toFloat() }
                ProgressMetric.WORKOUTS -> perDayMap[day]?.let { it.count.toFloat() }
                ProgressMetric.PRS -> prsMap[day]?.let { it.count.toFloat() }
            }
        }
        if (points.isEmpty()) {
            ChartEmpty()
            return@Card
        }
        val slotCount = daySlotCount(startDay, lastDay)
        val dsLabel = metricAxisLabel(metric, weightUnit)

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
                // Per day, everything but volume is a whole count: no 0.7-step axis labels.
                chart.axisLeft.isGranularityEnabled = metric != ProgressMetric.WEIGHT
                chart.axisLeft.granularity = 1f
                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val idx = value.toInt()
                        if (idx < 0 || idx >= slotCount) return ""
                        return dateFormat.format(Date(addDays(startDay, idx)))
                    }
                }
                chart.xAxis.axisMinimum = 0f
                chart.xAxis.axisMaximum = (slotCount - 1).coerceAtLeast(1).toFloat()
                chart.xAxis.labelCount = 6
                // Tap a point to read its exact value.
                chart.marker = ChartMarkerView(chart.context, onSurface, markerBg, markerStroke) { entry, _ ->
                    val day = addDays(startDay, entry.x.toInt())
                    "${dateFormat.format(Date(day))}  ·  ${formatMetricValue(chart.context.resources, entry.y, metric, weightUnit)}"
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
        // A muscle with no PRs has no bar to draw under the PRs metric.
        val sorted = muscle
            .filter { metric != ProgressMetric.PRS || it.prCount > 0 }
            .sortedByDescending { metricValue(it, metric, weightUnit) }
        if (sorted.isEmpty()) {
            ChartEmpty()
            return@Card
        }

        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val muted = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f).toArgb()
        val markerBg = MaterialTheme.colorScheme.surfaceContainerHighest.toArgb()
        val markerStroke = MaterialTheme.colorScheme.outline.toArgb()

        val labels = sorted.map { muscleLabel(it.muscleGroup) }
        val barColors = sorted.map { mv ->
            if (selected == null || selected.name == mv.muscleGroup) primary else muted
        }
        val entries = sorted.mapIndexed { idx, mv ->
            BarEntry(idx.toFloat(), metricValue(mv, metric, weightUnit))
        }
        val dsLabel = metricAxisLabel(metric, weightUnit)
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
                // Workouts and PRs are whole counts; don't let the axis step in fractions of one.
                // Sets by muscle can be halves (secondary muscles), so they keep a fine axis.
                chart.axisLeft.isGranularityEnabled =
                    metric == ProgressMetric.WORKOUTS || metric == ProgressMetric.PRS
                chart.axisLeft.granularity = 1f
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
                    "$name  ·  ${formatMetricValue(chart.context.resources, entry.y, metric, weightUnit)}"
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
        val dsLabel = metricAxisLabel(metric, weightUnit)

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
                    "$name  ·  ${formatMetricValue(chart.context.resources, entry.y, metric, weightUnit)}"
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

@Composable
private fun metricAxisLabel(metric: ProgressMetric, weightUnit: WeightUnit): String = when (metric) {
    ProgressMetric.WEIGHT -> stringResource(R.string.volume_with_unit, weightUnitLabel(weightUnit))
    else -> stringResource(metric.labelRes)
}

/**
 * The exact value, with its unit, for a tooltip or caption. Takes [Resources] rather than being
 * composable because chart markers call it from outside composition.
 */
private fun formatMetricValue(res: Resources, value: Float, metric: ProgressMetric, weightUnit: WeightUnit): String {
    val rounded = if (value % 1f == 0f) value.toInt().toString()
    else String.format(Locale.getDefault(), "%.1f", value)
    // Fractional counts (half sets from secondary muscles) always take the plural form.
    val quantity = if (value % 1f == 0f) value.toInt() else Int.MAX_VALUE
    return when (metric) {
        ProgressMetric.WEIGHT -> "$rounded ${weightUnitLabel(weightUnit)}"
        ProgressMetric.SETS -> res.getQuantityString(R.plurals.set_count_value, quantity, rounded)
        ProgressMetric.WORKOUTS -> res.getQuantityString(R.plurals.workout_count_value, quantity, rounded)
        ProgressMetric.PRS -> res.getQuantityString(R.plurals.pr_count_value, quantity, rounded)
    }
}

/** Display name for a muscle group stored by its enum name ("LOWER_BACK"). */
@Composable
private fun muscleLabel(raw: String): String =
    MuscleGroup.entries.firstOrNull { it.name == raw }?.label() ?: raw

private fun metricValue(mv: MuscleVolume, metric: ProgressMetric, weightUnit: WeightUnit): Float =
    when (metric) {
        ProgressMetric.WEIGHT -> kgToDisplay(mv.totalVolumeKg, weightUnit)
        ProgressMetric.SETS -> mv.totalSets
        ProgressMetric.WORKOUTS -> mv.workoutCount.toFloat()
        ProgressMetric.PRS -> mv.prCount.toFloat()
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
            text = stringResource(R.string.no_data_in_range),
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
    val dateFormat = rememberLocalizedDateFormat("MMMdyyyy")
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
                    stringResource(R.string.cardio_level_duration, formatWeightForDisplay(pr.maxWeightKg, weightUnit), formatDuration(pr.maxReps))
                } else if (pr.maxWeightKg <= 0f) {
                    pluralStringResource(R.plurals.rep_count, pr.maxReps, pr.maxReps)
                } else {
                    "${formatWeightForDisplay(pr.maxWeightKg, weightUnit)} ${weightUnitLabel(weightUnit)} × ${pr.maxReps}"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
