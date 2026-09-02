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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.SettingsEntryPoint
import com.strongest.app.utils.WorkoutRepositoryEntryPoint
import com.strongest.app.utils.displayToKg
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.weightUnitLabel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
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
        // The quantity is the total owned; a barbell loads pairs, so each side gets half.
        val maxPerSide = maxPerSide(maxCount, singleSide)
        var count = 0
        while (count < maxPerSide && remaining + epsilon >= plate) {
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

/** Competition plate colours, so the loaded bar reads at a glance like a real one. */
private fun plateColor(plate: Float, unit: WeightUnit): Color = if (unit == WeightUnit.KG) {
    when {
        plate >= 25f -> Color(0xFFD32F2F)
        plate >= 20f -> Color(0xFF1976D2)
        plate >= 15f -> Color(0xFFFBC02D)
        plate >= 10f -> Color(0xFF388E3C)
        plate >= 5f -> Color(0xFF9E9E9E)
        plate >= 2.5f -> Color(0xFFE57373)
        else -> Color(0xFF78909C)
    }
} else {
    when {
        plate >= 45f -> Color(0xFF1976D2)
        plate >= 35f -> Color(0xFFFBC02D)
        plate >= 25f -> Color(0xFF388E3C)
        plate >= 10f -> Color(0xFFD32F2F)
        plate >= 5f -> Color(0xFF9E9E9E)
        else -> Color(0xFF78909C)
    }
}

/** One plate drawn to scale; [heaviest] sets what a full-height plate weighs. */
@Composable
private fun PlateChip(plate: Float, unit: WeightUnit, heaviest: Float, width: Dp) {
    val fraction = (plate / heaviest.coerceAtLeast(0.01f)).coerceIn(0.28f, 1f)
    Box(
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .width(width)
            .height((24f + 44f * fraction).dp)
            .background(color = plateColor(plate, unit), shape = RoundedCornerShape(2.dp))
    )
}

/**
 * A loaded sleeve. Plates sit heaviest-against-the-collar, so the stack runs
 * heaviest-first outward — [reversed] draws it for the left sleeve, where outward is
 * leftward.
 */
@Composable
private fun PlateStack(load: BarLoad, unit: WeightUnit, heaviest: Float, width: Dp, reversed: Boolean) {
    val stack = if (reversed) load.loaded.reversed() else load.loaded
    for ((plate, count) in stack) {
        repeat(count) { PlateChip(plate, unit, heaviest, width) }
    }
}

@Composable
private fun BarSegment(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(2.dp)
            )
    )
}

/**
 * The loaded bar, seen from the side. A barbell is drawn whole — both sleeves loaded,
 * which is what you walk up to — while single-side mode shows the one sleeve you load.
 */
@Composable
private fun LoadedBarVisual(load: BarLoad, unit: WeightUnit, maxPlate: Float) {
    val plateCount = load.loaded.sumOf { it.second }
    // Both sleeves have to fit, so plates get thinner as the bar fills up.
    val plateWidth = when {
        plateCount > 7 -> 5.dp
        plateCount > 5 -> 7.dp
        plateCount > 3 -> 9.dp
        else -> 11.dp
    }
    val shaftWidth = when {
        plateCount > 7 -> 20.dp
        plateCount > 5 -> 28.dp
        else -> 40.dp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (load.isEmpty) {
            BarSegment(width = 56.dp, height = 8.dp)
            BarSegment(width = 6.dp, height = 22.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Empty bar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            BarSegment(width = 6.dp, height = 22.dp)
            BarSegment(width = 56.dp, height = 8.dp)
            return@Row
        }

        if (!load.singleSide) {
            // Left sleeve, loaded outward-in.
            BarSegment(width = 12.dp, height = 6.dp)
            PlateStack(load, unit, maxPlate, plateWidth, reversed = true)
            BarSegment(width = 5.dp, height = 22.dp)
        }
        BarSegment(width = shaftWidth, height = 8.dp)
        BarSegment(width = 5.dp, height = 22.dp)
        PlateStack(load, unit, maxPlate, plateWidth, reversed = false)
        BarSegment(width = 12.dp, height = 6.dp)
    }
}

@Composable
fun PlateCalculatorDialog(
    weightUnit: WeightUnit,
    initialTargetWeight: Float,
    onDismiss: () -> Unit,
    exerciseId: Long? = null
) {
    val unitLabel = weightUnitLabel(weightUnit)
    val defaultBars = if (weightUnit == WeightUnit.KG) KG_BARS else LBS_BARS
    val defaultPlates = if (weightUnit == WeightUnit.KG) STANDARD_KG_PLATES else STANDARD_LBS_PLATES

    val context = LocalContext.current
    val settingsRepo = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java).settingsRepository()
    }
    val workoutRepo = remember {
        EntryPointAccessors.fromApplication(context, WorkoutRepositoryEntryPoint::class.java)
            .workoutRepository()
    }
    val scope = rememberCoroutineScope()
    val appSettings by settingsRepo.settingsFlow.collectAsState(initial = null)

    var targetText by remember {
        mutableStateOf(
            TextFieldValue(if (initialTargetWeight > 0f) formatWeight(initialTargetWeight) else "")
        )
    }
    var targetFocused by remember { mutableStateOf(false) }
    LaunchedEffect(targetFocused) {
        if (targetFocused) {
            targetText = targetText.copy(selection = TextRange(0, targetText.text.length))
        }
    }

    // What the gym has, from Settings; 999 stands for "unlimited".
    var ownedPlates by remember(weightUnit) { mutableStateOf(defaultPlates.associateWith { 999 }) }
    var didInitFromSettings by remember(weightUnit) { mutableStateOf(false) }

    var load by remember(weightUnit) { mutableStateOf(BarLoad(bar = defaultBars.first())) }

    LaunchedEffect(appSettings, weightUnit, exerciseId) {
        val s = appSettings
        if (s != null && !didInitFromSettings) {
            val cfg = if (weightUnit == WeightUnit.KG) s.availableKgPlates else s.availableLbsPlates
            val owned = defaultPlates.associateWith { plate ->
                cfg.entries.find { abs(it.key - plate) < 0.001f }?.value ?: 0
            }
            ownedPlates = owned
            didInitFromSettings = true

            // How this exercise was set up last time — a leg press stays "no bar,
            // single side" instead of being reconfigured every session.
            val remembered = exerciseId?.let { workoutRepo.getExerciseSettings(it) }
            val rememberedBar = remembered?.barWeightKg
                ?.let { kgToDisplay(it, weightUnit) }
                ?.let { stored -> defaultBars.firstOrNull { abs(it - stored) < 0.2f } }
            load = BarLoad(
                bar = rememberedBar ?: load.bar,
                singleSide = remembered?.plateSingleSide ?: false
            )

            // Start from the plates that hit the weight being logged, then let the
            // user add or pull plates from there.
            if (initialTargetWeight > 0f) {
                load = barLoadForTarget(initialTargetWeight, load.bar, owned.filterValues { it > 0 }, load.singleSide)
            }
        }
    }

    // Remember the bar and loading style for next time, but only once the dialog has
    // finished setting itself up, so the initial load is not written straight back.
    fun rememberSetup(updated: BarLoad) {
        load = updated
        val id = exerciseId ?: return
        if (!didInitFromSettings) return
        scope.launch {
            workoutRepo.savePlatePreferences(
                exerciseId = id,
                barWeightKg = displayToKg(updated.bar, weightUnit),
                singleSide = updated.singleSide
            )
        }
    }

    val target = targetText.text.toFloatOrNull() ?: 0f
    val availablePlates = ownedPlates.filterValues { it > 0 }
    val total = load.total
    val difference = total - target
    val heaviestOwned = availablePlates.keys.maxOrNull() ?: defaultPlates.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plate Calculator") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        val filtered = input.text.filter { it.isDigit() || it == '.' }
                        targetText = if (filtered == input.text) input else TextFieldValue(filtered)
                    },
                    label = { Text("Target weight ($unitLabel, optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { targetFocused = it.isFocused }
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
                        val selected = abs(bar - load.bar) < 0.001f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { rememberSetup(load.copy(bar = bar)) }
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            rememberSetup(
                                load.copy(singleSide = !load.singleSide)
                                    .constrainedTo(availablePlates)
                            )
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = load.singleSide,
                        onCheckedChange = {
                            rememberSetup(load.copy(singleSide = it).constrainedTo(availablePlates))
                        }
                    )
                    Text("Single side (e.g. machines with one pin)")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (load.singleSide) "Add plates" else "Add plates (per side)",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                for (row in defaultPlates.chunked(2)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (plate in row) {
                            val owned = ownedPlates[plate] ?: 0
                            val count = load.countOf(plate)
                            val canAdd = load.canAdd(plate, owned)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 3.dp, horizontal = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = if (owned > 0) plateColor(plate, weightUnit)
                                                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = formatWeight(plate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (owned > 0) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.weight(1f)
                                )
                                StepperButton(
                                    label = "−",
                                    enabled = count > 0,
                                    onClick = { load = load.remove(plate) }
                                )
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.width(22.dp),
                                    textAlign = TextAlign.Center
                                )
                                StepperButton(
                                    label = "+",
                                    enabled = canAdd,
                                    onClick = { load = load.add(plate, owned) }
                                )
                            }
                        }
                        if (row.size == 1) {
                            Row(modifier = Modifier.weight(1f)) {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            load = barLoadForTarget(target, load.bar, availablePlates, load.singleSide)
                        },
                        enabled = target > 0f
                    ) { Text("Fill to target") }
                    TextButton(
                        onClick = { load = load.cleared() },
                        enabled = !load.isEmpty
                    ) { Text("Clear bar") }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                LoadedBarVisual(load = load, unit = weightUnit, maxPlate = heaviestOwned)

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total on bar: ${formatWeight(total)} $unitLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!load.singleSide && !load.isEmpty) {
                    Text(
                        text = "${formatWeight(load.plateWeight)} $unitLabel per side + " +
                            "${formatWeight(load.bar)} $unitLabel bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (target > 0f) {
                    Text(
                        text = when {
                            abs(difference) < 0.01f -> "Matches your target of ${formatWeight(target)} $unitLabel"
                            difference > 0f ->
                                "${formatWeight(difference)} $unitLabel over the ${formatWeight(target)} $unitLabel target"
                            else ->
                                "${formatWeight(-difference)} $unitLabel to go to ${formatWeight(target)} $unitLabel"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (abs(difference) < 0.01f) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Plate availability comes from Settings → Plate Calculator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(
                color = if (enabled) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
