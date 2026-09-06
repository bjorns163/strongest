package com.strongest.app.ui.settings

import com.strongest.app.BuildConfig
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.ThemeMode
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.OneRmFormula
import com.strongest.app.data.repository.RECOVERABLE_MUSCLE_GROUPS
import com.strongest.app.data.repository.Sex
import com.strongest.app.data.repository.defaultRecoveryHours
import com.strongest.app.data.repository.exerciseSeedData
import com.strongest.app.data.repository.WeightUnit
import java.util.Locale
import com.strongest.app.ui.components.DurationInputField
import com.strongest.app.ui.workout.STANDARD_KG_PLATES
import com.strongest.app.ui.workout.STANDARD_LBS_PLATES
import com.strongest.app.utils.formatDuration
import com.strongest.app.utils.weightUnitLabel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showTimerDialog by remember { mutableStateOf(value = false) }
    var timerSecondsInput by remember { mutableIntStateOf(90) }
    var showAdjustDialog by remember { mutableStateOf(value = false) }
    var adjustInput by remember { mutableStateOf(value = "") }
    var showLastSetTimerDialog by remember { mutableStateOf(value = false) }
    var lastSetTimerSecondsInput by remember { mutableIntStateOf(150) }
    var editingRecoveryMuscle by remember { mutableStateOf<MuscleGroup?>(null) }
    var recoveryInput by remember { mutableStateOf(value = "") }
    var showBirthYearDialog by remember { mutableStateOf(value = false) }
    var birthYearInput by remember { mutableStateOf(value = "") }

    // The two long lists start folded away: between them they are more rows than the rest of the
    // screen put together, and neither is something you come here to change often.
    var platesExpanded by remember { mutableStateOf(value = false) }
    var recoveryExpanded by remember { mutableStateOf(value = false) }

    LaunchedEffect(uiState.birthYear) {
        if (!showBirthYearDialog) {
            birthYearInput = if (uiState.birthYear > 0) uiState.birthYear.toString() else ""
        }
    }

    LaunchedEffect(uiState.timerAdjustmentSeconds) {
        if (!showAdjustDialog) {
            adjustInput = uiState.timerAdjustmentSeconds.toString()
        }
    }

    val context = LocalContext.current
    val exportImportResult by viewModel.exportImportResult.collectAsState()
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(exportImportResult) {
        when (val result = exportImportResult) {
            is ExportImportResult.ExportSuccess -> {
                snackbarHostState.showSnackbar("Data exported successfully")
                viewModel.resetExportImportResult()
            }
            is ExportImportResult.ImportSuccess -> {
                snackbarHostState.showSnackbar(
                    "Imported ${result.workoutCount} workouts, ${result.exerciseCount} exercises"
                )
                viewModel.resetExportImportResult()
            }
            is ExportImportResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.resetExportImportResult()
            }
            else -> {}
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportData(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importData(context, uri)
        }
    }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.let { data ->
                IntentCompat.getParcelableExtra(data, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            }
            viewModel.setNotificationSoundUri(uri?.toString())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsGroup(title = "General") {
                    SettingsChoiceRow(
                        title = "Theme",
                        selected = uiState.themeMode,
                        options = ThemeMode.entries,
                        optionLabel = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                        onSelect = { viewModel.setThemeMode(it) }
                    )
                    SettingsChoiceRow(
                        title = "Weight unit",
                        description = "Used for every weight you enter and see.",
                        selected = uiState.weightUnit,
                        options = listOf(WeightUnit.KG, WeightUnit.LBS),
                        optionLabel = { if (it == WeightUnit.KG) "Kilograms (kg)" else "Pounds (lbs)" },
                        valueLabel = { weightUnitLabel(it) },
                        onSelect = { viewModel.setWeightUnit(it) }
                    )
                }
            }

            item {
                // The alert sound belongs with the timer it fires for, not with the workout toggles.
                SettingsGroup(title = "Rest timer") {
                    SettingsRow(
                        title = "Default rest",
                        onClick = {
                            timerSecondsInput = uiState.defaultRestSeconds
                            showTimerDialog = true
                        },
                        trailing = { SettingsValue(formatDuration(uiState.defaultRestSeconds)) }
                    )
                    SettingsRow(
                        title = "Last set rest",
                        description = "Applied to the final set of an exercise.",
                        onClick = {
                            lastSetTimerSecondsInput = uiState.lastSetRestSeconds
                            showLastSetTimerDialog = true
                        },
                        trailing = { SettingsValue(formatDuration(uiState.lastSetRestSeconds)) }
                    )
                    SettingsRow(
                        title = "Adjustment step",
                        description = "How much the +/- buttons move a running timer.",
                        onClick = { showAdjustDialog = true },
                        trailing = { SettingsValue("${uiState.timerAdjustmentSeconds}s") }
                    )
                    SettingsRow(
                        title = "Alert sound",
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Timer Sound")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uiState.notificationSoundUri?.toUri())
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                            }
                            soundPickerLauncher.launch(intent)
                        },
                        trailing = {
                            SettingsValue(if (uiState.notificationSoundUri != null) "Custom" else "Default")
                        }
                    )
                }
            }

            item {
                SettingsGroup(title = "Workout") {
                    SettingsSwitchRow(
                        title = "Keep screen on",
                        description = "Stops the screen sleeping during an active workout.",
                        checked = uiState.keepScreenOn,
                        onCheckedChange = { viewModel.setKeepScreenOn(it) }
                    )
                    SettingsSwitchRow(
                        title = "Rate sets with RPE",
                        description = "Rate each set 1–10 for perceived exertion. Cardio sets are excluded.",
                        checked = uiState.rpeTrackingEnabled,
                        onCheckedChange = { viewModel.setRpeTrackingEnabled(it) }
                    )
                    SettingsSwitchRow(
                        title = "Notification bar",
                        description = "Shows workout progress, the rest timer and quick controls while you train.",
                        checked = uiState.workoutNotificationEnabled,
                        onCheckedChange = { viewModel.setWorkoutNotificationEnabled(it) }
                    )
                }
            }

            item {
                SettingsGroup(title = "Calculators") {
                    SettingsChoiceRow(
                        title = "1RM formula",
                        description = "Used by the 1RM calculator on the exercise detail screen.",
                        selected = uiState.oneRmFormula,
                        options = OneRmFormula.entries,
                        optionLabel = { formula ->
                            when (formula) {
                                OneRmFormula.EPLEY -> "Epley"
                                OneRmFormula.BRZYCKI -> "Brzycki"
                                OneRmFormula.BOTH -> "Both (average)"
                            }
                        },
                        onSelect = { viewModel.setOneRmFormula(it) }
                    )
                    SettingsExpanderRow(
                        title = "Available plates",
                        description = "How many of each plate your gym has. The calculator only loads plates you own.",
                        expanded = platesExpanded,
                        onToggle = { platesExpanded = !platesExpanded }
                    )
                    if (platesExpanded) {
                        PlateInventory(
                            weightUnit = uiState.weightUnit,
                            availableKgPlates = uiState.availableKgPlates,
                            availableLbsPlates = uiState.availableLbsPlates,
                            onPlatesChange = { unit, plates -> viewModel.setAvailablePlates(unit, plates) }
                        )
                    }
                }
            }

            item {
                SettingsGroup(title = "Recovery") {
                    SettingsExpanderRow(
                        title = "Recovery windows",
                        description = "How long each muscle needs before it counts as fresh on the Progress tab.",
                        expanded = recoveryExpanded,
                        onToggle = { recoveryExpanded = !recoveryExpanded }
                    )
                    if (recoveryExpanded) {
                        SettingsSubDivider()
                        RECOVERABLE_MUSCLE_GROUPS.forEach { muscle ->
                            val hours = uiState.recoveryHoursByMuscle[muscle] ?: defaultRecoveryHours(muscle)
                            SettingsRow(
                                title = muscleLabel(muscle),
                                dense = true,
                                onClick = {
                                    recoveryInput = hours.toString()
                                    editingRecoveryMuscle = muscle
                                },
                                trailing = { SettingsValue("${hours}h") }
                            )
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = "Profile") {
                    SettingsChoiceRow(
                        title = "Gender",
                        selected = uiState.userSex,
                        options = listOf(Sex.MALE, Sex.FEMALE, Sex.UNSET),
                        optionLabel = { sex ->
                            when (sex) {
                                Sex.MALE -> "Male"
                                Sex.FEMALE -> "Female"
                                Sex.UNSET -> "Not set"
                            }
                        },
                        onSelect = { viewModel.setUserSex(it) }
                    )
                    SettingsRow(
                        title = "Birth year",
                        onClick = { showBirthYearDialog = true },
                        trailing = {
                            SettingsValue(if (uiState.birthYear > 0) uiState.birthYear.toString() else "Not set")
                        }
                    )
                    SettingsChoiceRow(
                        title = "Caliper method",
                        description = "Skinfold protocol used by the guided body-fat measurement.",
                        selected = uiState.caliperMode,
                        options = CaliperMode.entries,
                        optionLabel = { it.label },
                        onSelect = { viewModel.setCaliperMode(it) }
                    )
                }
            }

            item {
                SettingsGroup(title = "Data") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportLauncher.launch("strongest_export.json") },
                            modifier = Modifier.weight(1f),
                            enabled = exportImportResult !is ExportImportResult.InProgress
                        ) {
                            if (exportImportResult is ExportImportResult.InProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Export")
                            }
                        }
                        OutlinedButton(
                            onClick = { showImportConfirmDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = exportImportResult !is ExportImportResult.InProgress
                        ) {
                            Text("Import")
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = "About") {
                    SettingsRow(
                        title = "Strongest",
                        description = "Free workout tracker with ${exerciseSeedData.size} exercises.",
                        trailing = { SettingsValue("v${BuildConfig.VERSION_NAME}") }
                    )
                    SettingsRow(
                        title = "Support development",
                        description = "Opens the project on GitHub.",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/bjorns163/strongest".toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Default Rest Timer") },
            text = {
                DurationInputField(
                    totalSeconds = timerSecondsInput,
                    onValueChange = { timerSecondsInput = it },
                    label = "Rest (mm:ss)"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDefaultRestSeconds(timerSecondsInput)
                        showTimerDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingRecoveryMuscle?.let { muscle ->
        AlertDialog(
            onDismissRequest = { editingRecoveryMuscle = null },
            title = { Text("${muscleLabel(muscle)} recovery") },
            text = {
                OutlinedTextField(
                    value = recoveryInput,
                    onValueChange = { recoveryInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Hours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        recoveryInput.toIntOrNull()?.let { hours ->
                            if (hours in 1..336) {
                                viewModel.setRecoveryHoursForMuscle(muscle, hours)
                            }
                        }
                        editingRecoveryMuscle = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecoveryMuscle = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBirthYearDialog) {
        AlertDialog(
            onDismissRequest = { showBirthYearDialog = false },
            title = { Text("Birth Year") },
            text = {
                OutlinedTextField(
                    value = birthYearInput,
                    onValueChange = { birthYearInput = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Year (e.g. 1990)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        birthYearInput.toIntOrNull()?.let { year ->
                            if (year in 1900..currentYear) {
                                viewModel.setBirthYear(year)
                            }
                        }
                        showBirthYearDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthYearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAdjustDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = { Text("Timer Adjustment Step") },
            text = {
                OutlinedTextField(
                    value = adjustInput,
                    onValueChange = { adjustInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Seconds") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        adjustInput.toIntOrNull()?.let { seconds ->
                            if (seconds in 5..300) {
                                viewModel.setTimerAdjustmentSeconds(seconds)
                            }
                        }
                        showAdjustDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLastSetTimerDialog) {
        AlertDialog(
            onDismissRequest = { showLastSetTimerDialog = false },
            title = { Text("Last Set Rest Timer") },
            text = {
                DurationInputField(
                    totalSeconds = lastSetTimerSecondsInput,
                    onValueChange = { lastSetTimerSecondsInput = it },
                    label = "Rest (mm:ss)"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setLastSetRestSeconds(lastSetTimerSecondsInput)
                        showLastSetTimerDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLastSetTimerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("Import Data") },
            text = {
                Text(
                    "This will REPLACE all existing data in the app with the imported data. " +
                    "This cannot be undone. Make sure you have exported your current data first " +
                    "if you want to keep it.\n\nContinue?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * A titled block of settings, drawn as one card.
 *
 * The screen had grown into a single flat run of rows under coloured headings, where the only
 * thing separating "Rest Timer" from "Workout" was a line of text. Giving each section its own
 * surface is what makes the list scannable again as it keeps growing.
 */
@Composable
private fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            // The palette's light background sits between surface and surfaceContainer, so a
            // filled card alone is nearly invisible there. The hairline edge is what actually
            // draws the group, and it holds up in both themes.
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(content = content)
        }
    }
}

/**
 * One setting: a label, optional supporting line, and whatever shows or changes the value.
 *
 * Every row in the screen goes through here so they all get the same height, padding and ripple —
 * previously each was hand-built, and rows ranged from comfortably tall to under the minimum
 * touch target depending on which ones had picked up a description along the way.
 */
@Composable
private fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    dense: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .heightIn(min = if (dense) 44.dp else 56.dp)
            .padding(horizontal = 16.dp, vertical = if (dense) 6.dp else 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        trailing?.invoke()
    }
}

/** The current value of a setting, sitting at the end of its row. */
@Composable
private fun SettingsValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * A row that toggles. The whole row is the target, not just the switch.
 */
@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null
) {
    SettingsRow(
        title = title,
        description = description,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

/**
 * A row that picks one of a few values. [valueLabel] shortens the collapsed row where the full
 * menu wording would crowd the supporting line.
 *
 * These were filled buttons opening a dropdown, which put five loud primary-coloured blocks down
 * a screen whose other half showed its values as quiet text. Same interaction, same row shape as
 * everything else, so the eye has one thing to follow.
 */
@Composable
private fun <T> SettingsChoiceRow(
    title: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    description: String? = null,
    valueLabel: (T) -> String = optionLabel
) {
    var expanded by remember { mutableStateOf(false) }
    SettingsRow(
        title = title,
        description = description,
        onClick = { expanded = true },
        trailing = {
            // The menu anchors to this box rather than to the whole row, so it drops from the
            // value being changed instead of from the far side of the screen.
            Box {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingsValue(valueLabel(selected))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(220.dp)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionLabel(option)) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            },
                            trailingIcon = {
                                if (option == selected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

/** A row that folds a long list of settings open and shut. */
@Composable
private fun SettingsExpanderRow(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    description: String? = null
) {
    SettingsRow(
        title = title,
        description = description,
        onClick = onToggle,
        trailing = {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

/** Separates an expanded list from the row that opened it. */
@Composable
private fun SettingsSubDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/**
 * The per-plate counts for the plate calculator, in two columns.
 *
 * Only the plates for the active weight unit are shown: the kg and lbs inventories are stored
 * separately and switching units swaps which one you are editing.
 */
@Composable
private fun PlateInventory(
    weightUnit: WeightUnit,
    availableKgPlates: Map<Float, Int>,
    availableLbsPlates: Map<Float, Int>,
    onPlatesChange: (WeightUnit, Map<Float, Int>) -> Unit
) {
    val unitLabel = weightUnitLabel(weightUnit)
    val allPlates = if (weightUnit == WeightUnit.KG) STANDARD_KG_PLATES else STANDARD_LBS_PLATES
    val plateQtys = if (weightUnit == WeightUnit.KG) availableKgPlates else availableLbsPlates

    SettingsSubDivider()
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "999 means unlimited. For a barbell the calculator loads a pair at a time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        for (row in allPlates.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (plate in row) {
                    val currentQty = plateQtys.entries.find { kotlin.math.abs(it.key - plate) < 0.001f }?.value ?: 0
                    var editText by remember(plate, weightUnit) { mutableStateOf(TextFieldValue(currentQty.toString())) }
                    var editFocused by remember(plate, weightUnit) { mutableStateOf(false) }

                    // Select all on focus so typing replaces the existing value.
                    LaunchedEffect(editFocused) {
                        if (editFocused) {
                            editText = editText.copy(selection = TextRange(0, editText.text.length))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatPlateLabel(plate) + " $unitLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { input ->
                                val filtered = input.text.filter { it.isDigit() }
                                editText = if (filtered == input.text) input else TextFieldValue(filtered)
                                val qty = filtered.toIntOrNull()
                                if (qty != null && qty >= 0) {
                                    val updated = plateQtys.toMutableMap()
                                    updated[plate] = qty
                                    onPlatesChange(weightUnit, updated)
                                }
                            },
                            modifier = Modifier
                                .width(72.dp)
                                .onFocusChanged { editFocused = it.isFocused },
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
    }
}

/** "LOWER_BACK" as "Lower back". */
private fun muscleLabel(muscle: MuscleGroup): String =
    muscle.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")

/**
 * Formats a plate weight for display, trimming trailing zeros (2.5 not 2.50).
 *
 * Kept out of the composable body: reading the default locale inside one is not observable by
 * Compose, so lint rejects it there.
 */
private fun formatPlateLabel(plate: Float): String = when {
    (plate % 1f) == 0f -> plate.toInt().toString()
    ((plate * 10f) % 1f) == 0f -> String.format(Locale.getDefault(), "%.1f", plate)
    else -> String.format(Locale.getDefault(), "%.2f", plate)
}
