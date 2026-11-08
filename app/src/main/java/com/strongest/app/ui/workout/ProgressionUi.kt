package com.strongest.app.ui.workout

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.strongest.app.BuildConfig
import com.strongest.app.R
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.repository.AppSettings
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.ProgressionConfig
import com.strongest.app.utils.ProgressionMethod
import com.strongest.app.utils.ProgressionSuggestion
import com.strongest.app.utils.SettingsEntryPoint
import com.strongest.app.utils.SuggestionKind
import com.strongest.app.utils.WorkoutRepositoryEntryPoint
import com.strongest.app.utils.defaultWeightStepKg
import com.strongest.app.utils.displayToKg
import com.strongest.app.utils.filterDecimalInput
import com.strongest.app.utils.formatDecimal
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.parseDecimalInput
import com.strongest.app.utils.weightUnitLabel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.util.Locale

const val GITHUB_REPO_URL = "https://github.com/bjorns163/strongest"
const val PROGRESSION_FEEDBACK_TEMPLATE = "progression-feedback.yml"

/**
 * A new GitHub issue on the progression feedback form, with what we know filled in. Issue forms
 * take field values by their id as query parameters.
 */
fun progressionFeedbackUrl(exerciseName: String? = null, suggestion: String? = null): String {
    val params = buildList {
        add("template" to PROGRESSION_FEEDBACK_TEMPLATE)
        if (!exerciseName.isNullOrBlank()) add("exercise" to exerciseName)
        if (!suggestion.isNullOrBlank()) add("suggestion" to suggestion)
        add("version" to BuildConfig.VERSION_NAME)
    }
    return "$GITHUB_REPO_URL/issues/new?" +
        params.joinToString("&") { (k, v) -> "$k=${Uri.encode(v)}" }
}

/** Opens [url] in the browser; quietly does nothing on a device without one. */
fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
    }
}

@Composable
fun progressionLoadText(weightKg: Float, reps: Int, unit: WeightUnit): String =
    if (weightKg <= 0f) {
        pluralStringResource(R.plurals.progression_reps_only, reps, reps)
    } else {
        stringResource(
            R.string.progression_load,
            "${formatWeightForDisplay(weightKg, unit)} ${weightUnitLabel(unit)}",
            reps
        )
    }

@Composable
fun progressionTitle(s: ProgressionSuggestion, unit: WeightUnit): String {
    val load = progressionLoadText(s.weightKg, s.reps, unit)
    return stringResource(
        when (s.kind) {
            SuggestionKind.INCREASE_WEIGHT -> R.string.progression_increase
            SuggestionKind.ADD_REPS -> R.string.progression_add_reps
            SuggestionKind.REPEAT -> R.string.progression_repeat
            SuggestionKind.DECREASE -> R.string.progression_decrease
            SuggestionKind.DELOAD -> R.string.progression_deload
            SuggestionKind.LONG_BREAK -> R.string.progression_long_break
        },
        load
    )
}

private fun formatRpe(rpe: Float): String =
    if (rpe % 1f == 0f) rpe.toInt().toString()
    else String.format(Locale.getDefault(), "%.1f", rpe)

@Composable
fun progressionReason(s: ProgressionSuggestion, unit: WeightUnit): String {
    val b = s.basis
    return when (s.kind) {
        SuggestionKind.DELOAD ->
            pluralStringResource(R.plurals.progression_basis_stalled, b.stalledSessions, b.stalledSessions)
        SuggestionKind.LONG_BREAK ->
            pluralStringResource(R.plurals.progression_basis_break, b.daysAgo, b.daysAgo)
        else -> {
            val load = progressionLoadText(b.weightKg, b.reps, unit)
            if (b.avgRpe != null) {
                pluralStringResource(R.plurals.progression_basis_rpe, b.sets, load, b.sets, formatRpe(b.avgRpe))
            } else {
                pluralStringResource(R.plurals.progression_basis, b.sets, load, b.sets)
            }
        }
    }
}

/**
 * The suggestion under an exercise's name. Tapping fills it into the open working sets;
 * holding opens a menu with the same action and a way to report a bad suggestion.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgressionSuggestionChip(
    suggestion: ProgressionSuggestion,
    exerciseName: String,
    weightUnit: WeightUnit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val title = progressionTitle(suggestion, weightUnit)
    val reason = progressionReason(suggestion, weightUnit)
    val icon = when (suggestion.kind) {
        SuggestionKind.INCREASE_WEIGHT, SuggestionKind.ADD_REPS -> Icons.AutoMirrored.Filled.TrendingUp
        SuggestionKind.REPEAT -> Icons.Default.Repeat
        SuggestionKind.DECREASE -> Icons.AutoMirrored.Filled.TrendingDown
        SuggestionKind.DELOAD -> Icons.Default.SelfImprovement
        SuggestionKind.LONG_BREAK -> Icons.Default.Refresh
    }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onApply,
                    onLongClick = { showMenu = true },
                    onClickLabel = stringResource(R.string.progression_apply)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.labelLarge)
                    Text(
                        reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.progression_apply)) },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                onClick = {
                    showMenu = false
                    onApply()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.progression_feedback_wrong)) },
                leadingIcon = { Icon(Icons.Default.Feedback, null) },
                onClick = {
                    showMenu = false
                    openUrl(context, progressionFeedbackUrl(exerciseName, "$title — $reason"))
                }
            )
        }
    }
}

/** A routine exercise's own rep range and target RPE; all null = Settings defaults. */
data class RoutineProgressionTarget(
    val repMin: Int? = null,
    val repMax: Int? = null,
    val targetRpe: Int? = null
) {
    val usesDefaults: Boolean get() = repMin == null || repMax == null
}

/**
 * Edits how one exercise progresses: method and weight step (stored per exercise, for every
 * routine) and, when [routineTarget] is given, the rep range and target RPE for this routine.
 */
@Composable
fun ProgressionPreferencesDialog(
    exerciseId: Long,
    exerciseName: String,
    equipment: Equipment,
    weightUnit: WeightUnit,
    lastWeightKg: Float,
    routineTarget: RoutineProgressionTarget?,
    onSaveRoutineTarget: (RoutineProgressionTarget) -> Unit = {},
    onSaved: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java).settingsRepository()
    }
    val workoutRepository = remember {
        EntryPointAccessors.fromApplication(context, WorkoutRepositoryEntryPoint::class.java)
            .workoutRepository()
    }
    val appSettings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
    val defaults: ProgressionConfig = appSettings.progression
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(false) }
    var method by remember { mutableStateOf(ProgressionMethod.DOUBLE) }
    var stepText by remember { mutableStateOf("") }

    var useDefaults by remember { mutableStateOf(routineTarget?.usesDefaults ?: true) }
    var minText by remember { mutableStateOf(routineTarget?.repMin?.toString() ?: "") }
    var maxText by remember { mutableStateOf(routineTarget?.repMax?.toString() ?: "") }
    var rpe by remember { mutableStateOf(routineTarget?.targetRpe) }

    LaunchedEffect(exerciseId) {
        val prefs = workoutRepository.getExerciseSettings(exerciseId)
        method = prefs?.progressionMethod
            ?.let { name -> ProgressionMethod.entries.firstOrNull { it.name == name } }
            ?: ProgressionMethod.DOUBLE
        stepText = prefs?.weightStepKg?.let { formatDecimal(kgToDisplay(it, weightUnit)) } ?: ""
        loaded = true
    }
    // Opening with the defaults shown makes "turn defaults off" start from something sensible.
    LaunchedEffect(defaults) {
        if (minText.isEmpty()) minText = defaults.defaultRepMin.toString()
        if (maxText.isEmpty()) maxText = defaults.defaultRepMax.toString()
    }

    val repMin = minText.toIntOrNull()
    val repMax = maxText.toIntOrNull()
    val rangeValid = useDefaults || routineTarget == null ||
        (repMin != null && repMax != null && repMin in ProgressionConfig.REP_RANGE_LIMITS &&
            repMax in ProgressionConfig.REP_RANGE_LIMITS && repMin <= repMax)
    val stepValid = stepText.isBlank() || (parseDecimalInput(stepText)?.let { it >= 0f } == true)
    val autoStepKg = defaultWeightStepKg(equipment, weightUnit, lastWeightKg)
    val unitLabel = weightUnitLabel(weightUnit)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.progression_dialog_title, exerciseName)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (routineTarget != null) {
                    Text(
                        stringResource(R.string.progression_this_routine),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = useDefaults,
                                onClick = { useDefaults = !useDefaults },
                                role = Role.Switch
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(
                                R.string.progression_use_defaults,
                                stringResource(
                                    R.string.progression_range_summary,
                                    defaults.defaultRepMin, defaults.defaultRepMax, defaults.defaultTargetRpe
                                )
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = useDefaults, onCheckedChange = { useDefaults = it })
                    }
                    if (!useDefaults) {
                        Text(stringResource(R.string.progression_rep_range), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = minText,
                                onValueChange = { minText = it.filter(Char::isDigit).take(2) },
                                label = { Text(stringResource(R.string.progression_rep_min)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = maxText,
                                onValueChange = { maxText = it.filter(Char::isDigit).take(2) },
                                label = { Text(stringResource(R.string.progression_rep_max)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!rangeValid) {
                            Text(
                                stringResource(R.string.progression_range_invalid),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(stringResource(R.string.progression_target_rpe), style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val selectedRpe = rpe ?: defaults.defaultTargetRpe
                            ProgressionConfig.TARGET_RPE_OPTIONS.forEach { option ->
                                FilterChip(
                                    selected = selectedRpe == option,
                                    onClick = { rpe = option },
                                    label = { Text(option.toString()) }
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                Text(
                    stringResource(R.string.progression_this_exercise),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(stringResource(R.string.progression_method), style = MaterialTheme.typography.labelLarge)
                ProgressionMethod.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = method == option,
                                onClick = { method = option },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = method == option, onClick = null)
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                stringResource(
                                    when (option) {
                                        ProgressionMethod.DOUBLE -> R.string.progression_method_double
                                        ProgressionMethod.LINEAR -> R.string.progression_method_linear
                                        ProgressionMethod.OFF -> R.string.progression_method_off
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                stringResource(
                                    when (option) {
                                        ProgressionMethod.DOUBLE -> R.string.progression_method_double_desc
                                        ProgressionMethod.LINEAR -> R.string.progression_method_linear_desc
                                        ProgressionMethod.OFF -> R.string.progression_method_off_desc
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (method != ProgressionMethod.OFF) {
                    OutlinedTextField(
                        value = stepText,
                        onValueChange = { stepText = filterDecimalInput(it) },
                        label = { Text(stringResource(R.string.progression_weight_step, unitLabel)) },
                        singleLine = true,
                        isError = !stepValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = {
                            Text(
                                if (autoStepKg > 0f) {
                                    stringResource(
                                        R.string.progression_weight_step_auto,
                                        "${formatDecimal(kgToDisplay(autoStepKg, weightUnit))} $unitLabel"
                                    )
                                } else {
                                    stringResource(R.string.progression_weight_step_reps_only)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = loaded && rangeValid && stepValid,
                onClick = {
                    val stepKg = stepText.takeIf { it.isNotBlank() }
                        ?.let(::parseDecimalInput)
                        ?.let { displayToKg(it, weightUnit) }
                    if (routineTarget != null) {
                        onSaveRoutineTarget(
                            if (useDefaults) RoutineProgressionTarget()
                            else RoutineProgressionTarget(repMin, repMax, rpe ?: defaults.defaultTargetRpe)
                        )
                    }
                    scope.launch {
                        workoutRepository.saveProgressionPreferences(
                            exerciseId,
                            method.takeIf { it != ProgressionMethod.DOUBLE },
                            stepKg
                        )
                        onSaved()
                        onDismiss()
                    }
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
