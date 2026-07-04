package com.strongest.app.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
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
import com.strongest.app.ui.workout.STANDARD_KG_PLATES
import com.strongest.app.ui.workout.STANDARD_LBS_PLATES
import com.strongest.app.utils.weightUnitLabel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDropdown by remember { mutableStateOf(value = false) }
    var showUnitDropdown by remember { mutableStateOf(value = false) }
    var showTimerDialog by remember { mutableStateOf(value = false) }
    var timerInput by remember { mutableStateOf(value = "") }
    var showAdjustDialog by remember { mutableStateOf(value = false) }
    var adjustInput by remember { mutableStateOf(value = "") }
    var showLastSetTimerDialog by remember { mutableStateOf(value = false) }
    var lastSetTimerInput by remember { mutableStateOf(value = "") }
    var editingRecoveryMuscle by remember { mutableStateOf<MuscleGroup?>(null) }
    var recoveryInput by remember { mutableStateOf(value = "") }
    var showBirthYearDialog by remember { mutableStateOf(value = false) }
    var birthYearInput by remember { mutableStateOf(value = "") }

    LaunchedEffect(uiState.birthYear) {
        if (!showBirthYearDialog) {
            birthYearInput = if (uiState.birthYear > 0) uiState.birthYear.toString() else ""
        }
    }

    LaunchedEffect(uiState.defaultRestSeconds) {
        if (!showTimerDialog) {
            timerInput = uiState.defaultRestSeconds.toString()
        }
    }

    LaunchedEffect(uiState.timerAdjustmentSeconds) {
        if (!showAdjustDialog) {
            adjustInput = uiState.timerAdjustmentSeconds.toString()
        }
    }

    LaunchedEffect(uiState.lastSetRestSeconds) {
        if (!showLastSetTimerDialog) {
            lastSetTimerInput = uiState.lastSetRestSeconds.toString()
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
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                SettingSection(title = "Appearance")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Theme",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Button(onClick = { showThemeDropdown = true }) {
                        Text(uiState.themeMode.name.replaceFirstChar { it.uppercase() })
                    }
                    DropdownMenu(
                        expanded = showThemeDropdown,
                        onDismissRequest = { showThemeDropdown = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                SettingSection(title = "Units")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Weight Unit",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Button(onClick = { showUnitDropdown = true }) {
                        Text(if (uiState.weightUnit == WeightUnit.KG) "Kilograms (kg)" else "Pounds (lbs)")
                    }
                    DropdownMenu(
                        expanded = showUnitDropdown,
                        onDismissRequest = { showUnitDropdown = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Kilograms (kg)") },
                            onClick = {
                                viewModel.setWeightUnit(WeightUnit.KG)
                                showUnitDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pounds (lbs)") },
                            onClick = {
                                viewModel.setWeightUnit(WeightUnit.LBS)
                                showUnitDropdown = false
                            }
                        )
                    }
                }
            }

            item {
                SettingSection(title = "Rest Timer")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showTimerDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Default rest (seconds)")
                    Text(
                        text = "${uiState.defaultRestSeconds}s",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showAdjustDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Timer adjust +/- (seconds)")
                    Text(
                        text = "${uiState.timerAdjustmentSeconds}s",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showLastSetTimerDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Last set rest (seconds)")
                    Text(
                        text = "${uiState.lastSetRestSeconds}s",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                SettingSection(title = "Workout")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Keep screen on during workout")
                    Switch(
                        checked = uiState.keepScreenOn,
                        onCheckedChange = { viewModel.setKeepScreenOn(it) }
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rate sets with RPE")
                        Switch(
                            checked = uiState.rpeTrackingEnabled,
                            onCheckedChange = { viewModel.setRpeTrackingEnabled(it) }
                        )
                    }
                    Text(
                        text = "Advanced: rate each set 1–10 (Rate of Perceived Exertion) during workouts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notification bar during workout")
                        Switch(
                            checked = uiState.workoutNotificationEnabled,
                            onCheckedChange = { viewModel.setWorkoutNotificationEnabled(it) }
                        )
                    }
                    Text(
                        text = "Shows your active workout progress, rest timer, and quick controls in the notification bar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Timer Sound")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uiState.notificationSoundUri?.toUri())
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                            }
                            soundPickerLauncher.launch(intent)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Timer notification sound")
                    Text(
                        text = if (uiState.notificationSoundUri != null) "Custom" else "Default",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                SettingSection(title = "Plate Calculator")
            }

            item {
                val unit = uiState.weightUnit
                val unitLabel = weightUnitLabel(unit)
                val allPlates = if (unit == WeightUnit.KG) STANDARD_KG_PLATES else STANDARD_LBS_PLATES
                val plateQtys = if (unit == WeightUnit.KG) uiState.availableKgPlates else uiState.availableLbsPlates

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Set how many of each plate you own (999 = unlimited). The calculator will respect these limits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val rows = allPlates.chunked(2)
                    for (row in rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (plate in row) {
                                val currentQty = plateQtys.entries.find { kotlin.math.abs(it.key - plate) < 0.001f }?.value ?: 0
                                var editText by remember(plate, unit) { mutableStateOf(currentQty.toString()) }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            (plate % 1f) == 0f -> plate.toInt().toString()
                                            ((plate * 10f) % 1f) == 0f -> String.format("%.1f", plate)
                                            else -> String.format("%.2f", plate)
                                        } + " $unitLabel",
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = editText,
                                        onValueChange = { input ->
                                            val filtered = input.filter { it.isDigit() }
                                            editText = filtered
                                            val qty = filtered.toIntOrNull()
                                            if (qty != null && qty >= 0) {
                                                val updated = plateQtys.toMutableMap()
                                                updated[plate] = qty
                                                viewModel.setAvailablePlates(unit, updated)
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
                }
            }

            item {
                SettingSection(title = "1RM Calculator")
            }

            item {
                var showFormulaDropdown by remember { mutableStateOf(false) }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Formula")
                        Box {
                            Button(onClick = { showFormulaDropdown = true }) {
                                Text(
                                    when (uiState.oneRmFormula) {
                                        OneRmFormula.EPLEY -> "Epley"
                                        OneRmFormula.BRZYCKI -> "Brzycki"
                                        OneRmFormula.BOTH -> "Both"
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = showFormulaDropdown,
                                onDismissRequest = { showFormulaDropdown = false }
                            ) {
                                OneRmFormula.entries.forEach { f ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (f) {
                                                    OneRmFormula.EPLEY -> "Epley"
                                                    OneRmFormula.BRZYCKI -> "Brzycki"
                                                    OneRmFormula.BOTH -> "Both (average)"
                                                }
                                            )
                                        },
                                        onClick = {
                                            viewModel.setOneRmFormula(f)
                                            showFormulaDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Used by the 1RM calculator on the exercise detail screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SettingSection(title = "Recovery")
            }

            item {
                Text(
                    text = "Recovery time per muscle, shown on the Progress tab. Big groups need longer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(RECOVERABLE_MUSCLE_GROUPS) { muscle ->
                val hours = uiState.recoveryHoursByMuscle[muscle] ?: defaultRecoveryHours(muscle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            recoveryInput = hours.toString()
                            editingRecoveryMuscle = muscle
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        muscle.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
                    )
                    Text(
                        text = "${hours}h",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                SettingSection(title = "Profile")
            }

            item {
                var showSexDropdown by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gender")
                    Box {
                        Button(onClick = { showSexDropdown = true }) {
                            Text(
                                when (uiState.userSex) {
                                    Sex.MALE -> "Male"
                                    Sex.FEMALE -> "Female"
                                    Sex.UNSET -> "Not set"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showSexDropdown,
                            onDismissRequest = { showSexDropdown = false }
                        ) {
                            listOf(Sex.MALE, Sex.FEMALE).forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(if (s == Sex.MALE) "Male" else "Female") },
                                    onClick = {
                                        viewModel.setUserSex(s)
                                        showSexDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showBirthYearDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Birth year")
                    Text(
                        text = if (uiState.birthYear > 0) uiState.birthYear.toString() else "Not set",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            item {
                var showCaliperDropdown by remember { mutableStateOf(false) }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Caliper method")
                        Box {
                            Button(onClick = { showCaliperDropdown = true }) {
                                Text(uiState.caliperMode.label)
                            }
                            DropdownMenu(
                                expanded = showCaliperDropdown,
                                onDismissRequest = { showCaliperDropdown = false }
                            ) {
                                CaliperMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.label) },
                                        onClick = {
                                            viewModel.setCaliperMode(mode)
                                            showCaliperDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Skinfold protocol used by the guided body-fat measurement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SettingSection(title = "Data")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            exportLauncher.launch("strongest_export.json")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = exportImportResult !is ExportImportResult.InProgress
                    ) {
                        if (exportImportResult is ExportImportResult.InProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Export Data")
                        }
                    }
                    Button(
                        onClick = { showImportConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = exportImportResult !is ExportImportResult.InProgress
                    ) {
                        Text("Import Data")
                    }
                }
            }

            item {
                SettingSection(title = "About")
            }

            item {
                Text(
                    text = "Strongest v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Free workout tracker with ${exerciseSeedData.size} exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                val context = LocalContext.current
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bjorns163/strongest"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Support Development")
                }
            }
        }
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Default Rest Timer") },
            text = {
                OutlinedTextField(
                    value = timerInput,
                    onValueChange = { timerInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Seconds") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        timerInput.toIntOrNull()?.let { seconds ->
                            if (seconds in 1..600) {
                                viewModel.setDefaultRestSeconds(seconds)
                            }
                        }
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
        val muscleLabel = muscle.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
        AlertDialog(
            onDismissRequest = { editingRecoveryMuscle = null },
            title = { Text("$muscleLabel recovery") },
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
                OutlinedTextField(
                    value = lastSetTimerInput,
                    onValueChange = { lastSetTimerInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Seconds") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        lastSetTimerInput.toIntOrNull()?.let { seconds ->
                            if (seconds in 1..600) {
                                viewModel.setLastSetRestSeconds(seconds)
                            }
                        }
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

@Composable
fun SettingSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
