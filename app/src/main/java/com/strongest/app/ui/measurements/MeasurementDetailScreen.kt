package com.strongest.app.ui.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.strongest.app.data.model.BodyMetric
import com.strongest.app.data.model.MeasurementEntry
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.DAY_MS
import com.strongest.app.utils.dailyEntries
import com.strongest.app.utils.daySlotCount
import com.strongest.app.utils.localDayStart
import java.text.SimpleDateFormat
import java.util.Date

enum class MeasurementRange(val label: String, val days: Int?) {
    DAYS_7("7d", 7),
    DAYS_30("30d", 30),
    DAYS_90("90d", 90),
    MONTHS_6("6m", 183),
    YEAR_1("1y", 365),
    ALL_TIME("All", null)
}

@Composable
fun MeasurementDetailScreen(
    metric: BodyMetric,
    onBack: () -> Unit = {},
    viewModel: MeasurementDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(metric) { viewModel.load(metric) }
    val entries by viewModel.entries.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCaliperDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<MeasurementEntry?>(null) }
    var range by remember { mutableStateOf(MeasurementRange.DAYS_30) }
    val caliperProfile by viewModel.caliperProfile.collectAsState()

    if (showAddDialog) {
        AddMeasurementDialog(
            metric = metric,
            weightUnit = weightUnit,
            onDismiss = { showAddDialog = false },
            onSave = { value, timestamp, notes ->
                viewModel.addEntry(value, timestamp, notes)
                showAddDialog = false
            }
        )
    }

    if (showCaliperDialog) {
        CaliperBodyFatDialog(
            profile = caliperProfile,
            onSaveProfile = { sex, year -> viewModel.saveProfile(sex, year) },
            onSave = { percent, notes ->
                viewModel.addEntry(percent, System.currentTimeMillis(), notes)
                showCaliperDialog = false
            },
            onDismiss = { showCaliperDialog = false }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("How to measure: ${metric.displayName}") },
            text = { Text(metric.instructions) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Got it") }
            }
        )
    }

    if (pendingDelete != null) {
        val toDelete = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Delete this measurement? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(toDelete)
                    pendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (metric == BodyMetric.BODY_FAT) {
                    ExtendedFloatingActionButton(
                        onClick = { showCaliperDialog = true },
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.Straighten, null, Modifier.padding(end = 8.dp))
                        Text("Calipers")
                    }
                }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Add measurement")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                        }
                        Text(
                            text = metric.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, "How to measure")
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MeasurementRange.entries.size) { idx ->
                        val r = MeasurementRange.entries[idx]
                        FilterChip(
                            selected = range == r,
                            onClick = { range = r },
                            label = { Text(r.label) }
                        )
                    }
                }
            }
            item {
                ProgressChartCard(
                    metric = metric,
                    entries = entries,
                    range = range,
                    weightUnit = weightUnit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (entries.isEmpty()) {
                item {
                    Text(
                        text = "No measurements logged yet. Tap + to add your first entry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val sorted = entries.sortedByDescending { it.timestamp }
                items(sorted.size, key = { sorted[it].id }) { idx ->
                    HistoryRow(
                        entry = sorted[idx],
                        metric = metric,
                        weightUnit = weightUnit,
                        onDelete = { pendingDelete = sorted[idx] }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressChartCard(
    metric: BodyMetric,
    entries: List<MeasurementEntry>,
    range: MeasurementRange,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        if (entries.isEmpty()) {
            MeasurementChartEmpty("Add measurements to see your progress")
            return@Card
        }
        val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val dateFormat = SimpleDateFormat("MMM d", LocalConfiguration.current.locales[0])

        // The x-axis spans every calendar day in the range; days without a measurement simply
        // have no point, and the line connects across them.
        val lastDay = localDayStart(System.currentTimeMillis())
        val firstEntryDay = localDayStart(entries.minOf { it.timestamp })
        val startDay = range.days?.let { lastDay - (it - 1) * DAY_MS } ?: firstEntryDay
        if (startDay > lastDay) {
            MeasurementChartEmpty("No measurements in this range")
            return@Card
        }
        val byDay = entries.groupBy { localDayStart(it.timestamp) }
        val points = dailyEntries(startDay, lastDay) { day ->
            byDay[day]?.last()?.let { convertCanonicalToDisplay(metric, it.value, weightUnit) }
        }
        if (points.size < 2) {
            MeasurementChartEmpty(
                if (points.isEmpty()) "No measurements in this range"
                else "Add at least one more entry to see a trend"
            )
            return@Card
        }
        val slotCount = daySlotCount(startDay, lastDay)
        val unitLabel = metricUnitLabel(metric, weightUnit)

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
                    val ds = LineDataSet(points, "${metric.displayName} ($unitLabel)").apply {
                        color = primary
                        valueTextColor = onSurface
                        circleColors = listOf(primary)
                        setDrawValues(false)
                        lineWidth = 2f
                        circleRadius = 3f
                    }
                    chart.data = LineData(ds)
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            if (idx < 0 || idx >= slotCount) return ""
                            return dateFormat.format(Date(startDay + idx * DAY_MS))
                        }
                    }
                    chart.xAxis.axisMinimum = 0f
                    chart.xAxis.axisMaximum = (slotCount - 1).coerceAtLeast(1).toFloat()
                    chart.xAxis.labelCount = 6
                    chart.notifyDataSetChanged()
                    chart.invalidate()
                }
            )
    }
}

@Composable
private fun MeasurementChartEmpty(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryRow(
    entry: MeasurementEntry,
    metric: BodyMetric,
    weightUnit: WeightUnit,
    onDelete: () -> Unit
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (entry.notes.isNotEmpty()) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = formatMetricValue(metric, entry.value, weightUnit),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Delete entry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMeasurementDialog(
    metric: BodyMetric,
    weightUnit: WeightUnit,
    onDismiss: () -> Unit,
    onSave: (Float, Long, String) -> Unit
) {
    var valueText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var customDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val unit = metricUnitLabel(metric, weightUnit)
    val parsed = valueText.replace(',', '.').toFloatOrNull()
    val canSave = parsed != null && parsed > 0f
    val dateFormat = SimpleDateFormat("MMM d, yyyy", LocalConfiguration.current.locales[0])

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    customDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${metric.displayName}") },
        text = {
            Column {
                Text(
                    text = metric.instructions,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Value ($unit)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Date (optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (customDate != null) dateFormat.format(Date(customDate!!))
                            else "Today",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(if (customDate == null) "Pick" else "Change")
                    }
                    if (customDate != null) {
                        TextButton(onClick = { customDate = null }) { Text("Reset") }
                    }
                }
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val canonical = convertDisplayToCanonical(metric, parsed!!, weightUnit)
                    val ts = customDate ?: System.currentTimeMillis()
                    onSave(canonical, ts, notesText.trim())
                },
                enabled = canSave
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
