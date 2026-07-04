package com.strongest.app.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.SettingsEntryPoint
import com.strongest.app.utils.weightUnitLabel
import dagger.hilt.android.EntryPointAccessors
import kotlin.math.abs
import kotlin.math.roundToInt

private val KG_BARS = listOf(20f, 15f, 10f, 7f, 0f)
private val LBS_BARS = listOf(45f, 35f, 25f, 15f, 0f)

val STANDARD_KG_PLATES = listOf(25f, 20f, 15f, 10f, 5f, 2.5f, 1.25f, 0.75f, 0.5f, 0.25f)
val STANDARD_LBS_PLATES = listOf(45f, 35f, 25f, 10f, 5f, 2.5f, 1.25f)

data class PlateResult(
    val perSide: List<Pair<Float, Int>>,
    val achievedTotal: Float,
    val remainder: Float
)

fun calculatePlates(target: Float, bar: Float, availablePlates: Map<Float, Int>, singleSide: Boolean = false): PlateResult {
    val perSide = if (singleSide) (target - bar) else (target - bar) / 2f
    if (perSide <= 0f) {
        return PlateResult(emptyList(), bar.coerceAtMost(target), (target - bar).coerceAtLeast(0f))
    }
    val sorted = availablePlates.keys.sortedDescending()
    var remaining = perSide
    val counts = mutableListOf<Pair<Float, Int>>()
    val epsilon = 0.001f
    for (plate in sorted) {
        if (plate <= 0f) continue
        val maxCount = availablePlates[plate] ?: 0
        var count = 0
        while (count < maxCount && remaining + epsilon >= plate) {
            remaining -= plate
            count++
        }
        if (count > 0) counts.add(plate to count)
    }
    val achievedPerSide = perSide - remaining
    val achievedTotal = if (singleSide) bar + achievedPerSide else bar + (achievedPerSide * 2f)
    return PlateResult(counts, achievedTotal, if (singleSide) remaining else remaining * 2f)
}

private fun formatWeight(value: Float): String {
    val rounded = (value * 100f).roundToInt() / 100f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else {
        if ((rounded * 10f) % 1f == 0f) String.format("%.1f", rounded) else String.format("%.2f", rounded)
    }
}

@Composable
fun PlateCalculatorDialog(
    weightUnit: WeightUnit,
    initialTargetWeight: Float,
    onDismiss: () -> Unit
) {
    val unitLabel = weightUnitLabel(weightUnit)
    val defaultBars = if (weightUnit == WeightUnit.KG) KG_BARS else LBS_BARS
    val defaultPlates = if (weightUnit == WeightUnit.KG) STANDARD_KG_PLATES else STANDARD_LBS_PLATES

    val context = LocalContext.current
    val settingsRepo = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java).settingsRepository()
    }
    val appSettings by settingsRepo.settingsFlow.collectAsState(initial = null)

    var targetText by remember {
        mutableStateOf(
            if (initialTargetWeight > 0f) formatWeight(initialTargetWeight) else ""
        )
    }
    var selectedBar by remember { mutableStateOf(defaultBars.first()) }
    var singleSide by remember { mutableStateOf(false) }
    var plateQuantities by remember(weightUnit) {
        mutableStateOf(defaultPlates.associateWith { 999 })
    }
    var didInitFromSettings by remember(weightUnit) { mutableStateOf(false) }
    LaunchedEffect(appSettings, weightUnit) {
        val s = appSettings
        if (s != null && !didInitFromSettings) {
            val cfg = if (weightUnit == WeightUnit.KG) s.availableKgPlates else s.availableLbsPlates
            plateQuantities = defaultPlates.associateWith { plate ->
                cfg.entries.find { abs(it.key - plate) < 0.001f }?.value ?: 0
            }
            didInitFromSettings = true
        }
    }

    val target = targetText.toFloatOrNull() ?: 0f
    val activePlates = plateQuantities.filter { it.value > 0 }
    val result = remember(target, selectedBar, singleSide, activePlates) {
        calculatePlates(target, selectedBar, activePlates, singleSide)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plate Calculator") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        targetText = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Target weight ($unitLabel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Bar", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (bar in defaultBars) {
                        val selected = abs(bar - selectedBar) < 0.001f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedBar = bar }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (bar == 0f) "No bar" else "${formatWeight(bar)} $unitLabel",
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { singleSide = !singleSide }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = singleSide, onCheckedChange = { singleSide = it })
                    Text("Single side (e.g. machines with one pin)")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Available plates (0 = not owned)", style = MaterialTheme.typography.labelLarge)
                val plateRows = defaultPlates.chunked(2)
                for (row in plateRows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (plate in row) {
                            val qty = plateQuantities[plate] ?: 0
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${formatWeight(plate)} $unitLabel",
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = qty.toString(),
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }
                                        val newQty = filtered.toIntOrNull()
                                        if (newQty != null && newQty >= 0) {
                                            plateQuantities = plateQuantities.toMutableMap().apply {
                                                this[plate] = newQty
                                            }
                                        } else if (filtered.isEmpty()) {
                                            plateQuantities = plateQuantities.toMutableMap().apply {
                                                this[plate] = 0
                                            }
                                        }
                                    },
                                    modifier = Modifier.width(72.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (row.size == 1) {
                            Row(modifier = Modifier.weight(1f)) {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (singleSide) "Plates to add" else "Plates per side", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                if (target <= 0f) {
                    Text(
                        text = "Enter a target weight to see the plate breakdown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (target < selectedBar) {
                    Text(
                        text = "Target is lighter than the bar (${formatWeight(selectedBar)} $unitLabel).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (result.perSide.isEmpty()) {
                    Text(text = "Just the bar — no plates needed.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    for ((plate, count) in result.perSide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${formatWeight(plate)} $unitLabel × $count",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (target > 0f && target >= selectedBar) {
                    Text(
                        text = "Achievable total: ${formatWeight(result.achievedTotal)} $unitLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (result.remainder > 0.01f) {
                        Text(
                            text = "Short by ${formatWeight(result.remainder)} $unitLabel with the selected plates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
