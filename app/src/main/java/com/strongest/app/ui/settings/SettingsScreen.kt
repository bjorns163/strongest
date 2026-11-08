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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.R
import com.strongest.app.ThemeMode
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.CaliperMode
import com.strongest.app.data.repository.OneRmFormula
import com.strongest.app.data.repository.RECOVERABLE_MUSCLE_GROUPS
import com.strongest.app.data.repository.SILENT_SOUND
import com.strongest.app.data.repository.Sex
import com.strongest.app.data.repository.defaultRecoveryHours
import com.strongest.app.data.repository.exerciseSeedData
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.ui.components.DurationInputField
import com.strongest.app.ui.components.label
import com.strongest.app.ui.workout.STANDARD_KG_PLATES
import com.strongest.app.ui.workout.STANDARD_LBS_PLATES
import com.strongest.app.utils.formatDecimal
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
    val resources = LocalResources.current
    val exportImportResult by viewModel.exportImportResult.collectAsState()
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(exportImportResult) {
        when (val result = exportImportResult) {
            is ExportImportResult.ExportSuccess -> {
                snackbarHostState.showSnackbar(resources.getString(R.string.data_exported))
                viewModel.resetExportImportResult()
            }
            is ExportImportResult.ImportSuccess -> {
                val workouts = resources.getQuantityString(
                    R.plurals.workout_count, result.workoutCount, result.workoutCount
                )
                val exercises = resources.getQuantityString(
                    R.plurals.exercise_count, result.exerciseCount, result.exerciseCount
                )
                snackbarHostState.showSnackbar(resources.getString(R.string.data_imported, workouts, exercises))
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
            // The picker reports Silent as null and Default as the system default URI.
            viewModel.setNotificationSoundUri(
                when {
                    uri == null -> SILENT_SOUND
                    RingtoneManager.isDefault(uri) -> null
                    else -> uri.toString()
                }
            )
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
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                SettingsGroup(title = stringResource(R.string.settings_general)) {
                    SettingsChoiceRow(
                        title = stringResource(R.string.settings_theme),
                        selected = uiState.themeMode,
                        options = ThemeMode.entries,
                        optionLabel = { mode ->
                            stringResource(
                                when (mode) {
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                    ThemeMode.SYSTEM -> R.string.theme_system
                                }
                            )
                        },
                        onSelect = { viewModel.setThemeMode(it) }
                    )
                    SettingsChoiceRow(
                        title = stringResource(R.string.settings_weight_unit),
                        description = stringResource(R.string.settings_weight_unit_desc),
                        selected = uiState.weightUnit,
                        options = listOf(WeightUnit.KG, WeightUnit.LBS),
                        optionLabel = { stringResource(if (it == WeightUnit.KG) R.string.kilograms else R.string.pounds) },
                        valueLabel = { weightUnitLabel(it) },
                        onSelect = { viewModel.setWeightUnit(it) }
                    )
                }
            }

            item {
                // The alert sound belongs with the timer it fires for, not with the workout toggles.
                SettingsGroup(title = stringResource(R.string.settings_rest_timer)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_default_rest),
                        onClick = {
                            timerSecondsInput = uiState.defaultRestSeconds
                            showTimerDialog = true
                        },
                        trailing = { SettingsValue(formatDuration(uiState.defaultRestSeconds)) }
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_last_set_rest),
                        description = stringResource(R.string.settings_last_set_rest_desc),
                        onClick = {
                            lastSetTimerSecondsInput = uiState.lastSetRestSeconds
                            showLastSetTimerDialog = true
                        },
                        trailing = { SettingsValue(formatDuration(uiState.lastSetRestSeconds)) }
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_adjustment_step),
                        description = stringResource(R.string.settings_adjustment_step_desc),
                        onClick = { showAdjustDialog = true },
                        trailing = { SettingsValue(stringResource(R.string.seconds_short, uiState.timerAdjustmentSeconds)) }
                    )
                    val soundPickerTitle = stringResource(R.string.select_timer_sound)
                    SettingsRow(
                        title = stringResource(R.string.settings_alert_sound),
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, soundPickerTitle)
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    when (val current = uiState.notificationSoundUri) {
                                        null -> android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                                        SILENT_SOUND -> null
                                        else -> current.toUri()
                                    }
                                )
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                            }
                            soundPickerLauncher.launch(intent)
                        },
                        trailing = {
                            SettingsValue(
                                stringResource(
                                    when (uiState.notificationSoundUri) {
                                        null -> R.string.sound_default
                                        SILENT_SOUND -> R.string.sound_silent
                                        else -> R.string.sound_custom
                                    }
                                )
                            )
                        }
                    )
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.nav_workout)) {
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_keep_screen_on),
                        description = stringResource(R.string.settings_keep_screen_on_desc),
                        checked = uiState.keepScreenOn,
                        onCheckedChange = { viewModel.setKeepScreenOn(it) }
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_rpe),
                        description = stringResource(R.string.settings_rpe_desc),
                        checked = uiState.rpeTrackingEnabled,
                        onCheckedChange = { viewModel.setRpeTrackingEnabled(it) }
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_notification),
                        description = stringResource(R.string.settings_notification_desc),
                        checked = uiState.workoutNotificationEnabled,
                        onCheckedChange = { viewModel.setWorkoutNotificationEnabled(it) }
                    )
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.settings_calculators)) {
                    SettingsChoiceRow(
                        title = stringResource(R.string.settings_1rm_formula),
                        description = stringResource(R.string.settings_1rm_formula_desc),
                        selected = uiState.oneRmFormula,
                        options = OneRmFormula.entries,
                        optionLabel = { formula ->
                            when (formula) {
                                OneRmFormula.EPLEY -> stringResource(R.string.formula_epley)
                                OneRmFormula.BRZYCKI -> stringResource(R.string.formula_brzycki)
                                OneRmFormula.BOTH -> stringResource(R.string.formula_both)
                            }
                        },
                        onSelect = { viewModel.setOneRmFormula(it) }
                    )
                    SettingsExpanderRow(
                        title = stringResource(R.string.settings_available_plates),
                        description = stringResource(R.string.settings_available_plates_desc),
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
                SettingsGroup(title = stringResource(R.string.settings_recovery)) {
                    SettingsExpanderRow(
                        title = stringResource(R.string.settings_recovery_windows),
                        description = stringResource(R.string.settings_recovery_windows_desc),
                        expanded = recoveryExpanded,
                        onToggle = { recoveryExpanded = !recoveryExpanded }
                    )
                    if (recoveryExpanded) {
                        SettingsSubDivider()
                        RECOVERABLE_MUSCLE_GROUPS.forEach { muscle ->
                            val hours = uiState.recoveryHoursByMuscle[muscle] ?: defaultRecoveryHours(muscle)
                            SettingsRow(
                                title = muscle.label(),
                                dense = true,
                                onClick = {
                                    recoveryInput = hours.toString()
                                    editingRecoveryMuscle = muscle
                                },
                                trailing = { SettingsValue(stringResource(R.string.hours_short, hours)) }
                            )
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.settings_profile)) {
                    SettingsChoiceRow(
                        title = stringResource(R.string.settings_gender),
                        selected = uiState.userSex,
                        options = listOf(Sex.MALE, Sex.FEMALE, Sex.UNSET),
                        optionLabel = { sex ->
                            when (sex) {
                                Sex.MALE -> stringResource(R.string.sex_male)
                                Sex.FEMALE -> stringResource(R.string.sex_female)
                                Sex.UNSET -> stringResource(R.string.not_set)
                            }
                        },
                        onSelect = { viewModel.setUserSex(it) }
                    )
                    SettingsRow(
                        title = stringResource(R.string.birth_year),
                        onClick = { showBirthYearDialog = true },
                        trailing = {
                            SettingsValue(if (uiState.birthYear > 0) uiState.birthYear.toString() else stringResource(R.string.not_set))
                        }
                    )
                    SettingsChoiceRow(
                        title = stringResource(R.string.settings_caliper_method),
                        description = stringResource(R.string.settings_caliper_method_desc),
                        selected = uiState.caliperMode,
                        options = CaliperMode.entries,
                        optionLabel = { stringResource(it.labelRes) },
                        onSelect = { viewModel.setCaliperMode(it) }
                    )
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.settings_data)) {
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
                                Text(stringResource(R.string.export))
                            }
                        }
                        OutlinedButton(
                            onClick = { showImportConfirmDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = exportImportResult !is ExportImportResult.InProgress
                        ) {
                            Text(stringResource(R.string.import_label))
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.settings_about)) {
                    SettingsRow(
                        title = stringResource(R.string.app_name),
                        description = pluralStringResource(R.plurals.settings_about_desc, exerciseSeedData.size, exerciseSeedData.size),
                        trailing = { SettingsValue("v${BuildConfig.VERSION_NAME}") }
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_support),
                        description = stringResource(R.string.settings_support_desc),
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
            title = { Text(stringResource(R.string.default_rest_timer_title)) },
            text = {
                DurationInputField(
                    totalSeconds = timerSecondsInput,
                    onValueChange = { timerSecondsInput = it },
                    label = stringResource(R.string.rest_mm_ss)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDefaultRestSeconds(timerSecondsInput)
                        showTimerDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    editingRecoveryMuscle?.let { muscle ->
        AlertDialog(
            onDismissRequest = { editingRecoveryMuscle = null },
            title = { Text(stringResource(R.string.muscle_recovery_title, muscle.label())) },
            text = {
                OutlinedTextField(
                    value = recoveryInput,
                    onValueChange = { recoveryInput = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.hours)) },
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecoveryMuscle = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showBirthYearDialog) {
        AlertDialog(
            onDismissRequest = { showBirthYearDialog = false },
            title = { Text(stringResource(R.string.birth_year_title)) },
            text = {
                OutlinedTextField(
                    value = birthYearInput,
                    onValueChange = { birthYearInput = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text(stringResource(R.string.year_hint)) },
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthYearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAdjustDialog) {
        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = { Text(stringResource(R.string.timer_adjustment_title)) },
            text = {
                OutlinedTextField(
                    value = adjustInput,
                    onValueChange = { adjustInput = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.seconds)) },
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showLastSetTimerDialog) {
        AlertDialog(
            onDismissRequest = { showLastSetTimerDialog = false },
            title = { Text(stringResource(R.string.last_set_rest_title)) },
            text = {
                DurationInputField(
                    totalSeconds = lastSetTimerSecondsInput,
                    onValueChange = { lastSetTimerSecondsInput = it },
                    label = stringResource(R.string.rest_mm_ss)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setLastSetRestSeconds(lastSetTimerSecondsInput)
                        showLastSetTimerDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLastSetTimerDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text(stringResource(R.string.import_data_title)) },
            text = {
                Text(
                    stringResource(R.string.import_data_message)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                ) {
                    Text(stringResource(R.string.continue_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    description: String? = null,
    valueLabel: @Composable (T) -> String = optionLabel
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
                                        contentDescription = stringResource(R.string.selected),
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
                contentDescription = stringResource(if (expanded) R.string.collapse else R.string.expand),
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
            text = stringResource(R.string.plates_unlimited_hint),
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

/**
 * Formats a plate weight for display, trimming trailing zeros (2.5 not 2.50).
 *
 * Kept out of the composable body: reading the default locale inside one is not observable by
 * Compose, so lint rejects it there.
 */
private fun formatPlateLabel(plate: Float): String = formatDecimal(plate)
