package com.strongest.app.ui.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.model.SetType
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.ui.components.DurationTextField
import com.strongest.app.ui.components.reopenKeyboardOnTap
import com.strongest.app.ui.exercise.ExercisePickerResultHolder
import com.strongest.app.ui.theme.LocalSuccessColor
import com.strongest.app.utils.formatDecimal
import com.strongest.app.utils.displayToKg
import com.strongest.app.utils.formatDuration
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.parseDecimalInput
import com.strongest.app.utils.rememberWeightUnit
import com.strongest.app.utils.weightUnitLabel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.strongest.app.R
import com.strongest.app.ui.components.label
import com.strongest.app.utils.rememberLocalizedDateFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ActiveWorkoutEntryPoint {
    fun exercisePickerResultHolder(): ExercisePickerResultHolder
}

@Composable
fun ActiveWorkoutScreen(
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onNavigateToReplacePicker: (() -> Unit)? = null,
    onViewExerciseDetail: (exerciseId: Long, workoutExerciseId: Long?) -> Unit = { _, _ -> },
    viewModel: ActiveWorkoutViewModel = hiltViewModel(),
    initialWorkoutId: Long? = null,
    resumeWorkoutId: Long? = null,
    initialRoutineId: Long? = null,
    warmUpSetsToAdd: com.strongest.app.ui.navigation.AddWarmUpSetsRequest? = null,
    onWarmUpSetsConsumed: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val prs by viewModel.workoutPrs.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }

    var showNotificationRationale by remember { mutableStateOf(false) }
    var canStartWorkout by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || initialWorkoutId != null
        )
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // The workout was deleted while this screen sat in a tab's back stack: leave rather than
    // fall through to the start-a-new-workout branch below.
    LaunchedEffect(state.workoutDeleted) {
        if (state.workoutDeleted) onBack()
    }

    LaunchedEffect(canStartWorkout) {
        if (!canStartWorkout || state.workoutDeleted) return@LaunchedEffect
        if (resumeWorkoutId != null) {
            viewModel.loadWorkout(resumeWorkoutId)
        } else if (initialWorkoutId != null) {
            viewModel.loadCompletedWorkout(initialWorkoutId)
        } else if (state.workoutId == null) {
            if (initialRoutineId != null) {
                viewModel.startWorkoutFromRoutine(initialRoutineId)
            } else {
                viewModel.startNewWorkoutIfNeeded()
            }
        }
    }

    LaunchedEffect(warmUpSetsToAdd) {
        val request = warmUpSetsToAdd ?: return@LaunchedEffect
        val workoutExerciseId = request.workoutExerciseId
        if (workoutExerciseId != null && request.sets.isNotEmpty()) {
            viewModel.addWarmUpSets(workoutExerciseId, request.sets)
        }
        onWarmUpSetsConsumed()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            initialWorkoutId == null &&
            state.workoutNotificationEnabled
        ) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                canStartWorkout = true
            } else {
                val activity = context as? Activity
                if (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {
                    showNotificationRationale = true
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    canStartWorkout = true
                }
            }
        } else {
            canStartWorkout = true
        }
    }
    val exercisePickerResultHolder = remember {
        EntryPointAccessors.fromApplication(
            context,
            ActiveWorkoutEntryPoint::class.java
        ).exercisePickerResultHolder()
    }
    val pickerResult by exercisePickerResultHolder.result.collectAsState()
    LaunchedEffect(pickerResult) {
        val exerciseIds = pickerResult?.takeIf { it.isNotEmpty() } ?: return@LaunchedEffect
        val replaceTarget = exercisePickerResultHolder.replacingExerciseId
            ?.takeIf { exercisePickerResultHolder.isReplaceMode }
        // Drain the holder before invoking the VM so a re-entrant trigger can't replay this result.
        exercisePickerResultHolder.consume()
        exercisePickerResultHolder.clearReplaceMode()
        if (replaceTarget != null) {
            viewModel.replaceExercise(replaceTarget, exerciseIds.first())
        } else {
            viewModel.addExercises(exerciseIds)
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val weightUnit by rememberWeightUnit()

    val currentView = LocalView.current
    DisposableEffect(state.keepScreenOn) {
        currentView.keepScreenOn = state.keepScreenOn
        onDispose { currentView.keepScreenOn = false }
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = {
                showNotificationRationale = false
                canStartWorkout = true
            },
            title = { Text(stringResource(R.string.notification_permission_title)) },
            text = {
                Text(
                    stringResource(R.string.notification_permission_message)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationRationale = false
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    canStartWorkout = true
                }) {
                    Text(stringResource(R.string.allow))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNotificationRationale = false
                    canStartWorkout = true
                }) {
                    Text(stringResource(R.string.not_now))
                }
            }
        )
    }

    if (state.isFinished) {
        WorkoutFinishedScreen(onBack = onBack)
        return
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.cancel_workout)) },
            text = { Text(stringResource(R.string.cancel_workout_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelWorkout(onCancelled = onBack)
                    }
                ) {
                    Text(stringResource(R.string.cancel_workout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.continue_workout))
                }
            }
        )
    }

    if (state.showFinishDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFinishDialog() },
            title = { Text(stringResource(R.string.finish_workout)) },
            text = {
                Text(pluralStringResource(R.plurals.uncompleted_sets_message, state.uncompletedSetsCount, state.uncompletedSetsCount))
            },
            confirmButton = {
                TextButton(onClick = { viewModel.markUncompletedAsDone() }) {
                    Text(stringResource(R.string.mark_all_completed))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.discardUncompletedSets() }) {
                    Text(stringResource(R.string.discard_unfinished), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    if (state.showExistingWorkoutDialog) {
        AlertDialog(
            onDismissRequest = { onBack() },
            title = { Text(stringResource(R.string.active_workout_found)) },
            text = { Text(stringResource(R.string.active_workout_found_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resumeExistingWorkout() }) {
                    Text(stringResource(R.string.resume))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.discardExistingAndStartNew() }) {
                    Text(stringResource(R.string.cancel_and_start_new), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    if (state.showRoutineSaveDialog) {
        val fromRoutine = state.sourceRoutineId != null && state.sourceRoutineExists
        AlertDialog(
            onDismissRequest = { viewModel.dismissRoutineSaveDialog() },
            title = { Text(stringResource(if (fromRoutine) R.string.save_routine_changes else R.string.save_as_routine)) },
            text = {
                Column {
                    if (fromRoutine) {
                        if (!state.hasStructuralChanges) {
                            TextButton(
                                onClick = { viewModel.updateRoutineSetsOnlyAndFinish() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.update_sets_only), modifier = Modifier.fillMaxWidth())
                            }
                        } else {
                            TextButton(
                                onClick = { viewModel.updateRoutineFullAndFinish() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.update_full_routine), modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    TextButton(
                        onClick = { viewModel.showSaveAsNewRoutineDialog() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.save_as_new_routine), modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRoutineSaveDialog() }) {
                    Text(stringResource(R.string.just_finish))
                }
            }
        )
    }

    if (state.showNewRoutineNameDialog) {
        var newRoutineName by remember { mutableStateOf(state.workoutName ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissNewRoutineNameDialog() },
            title = { Text(stringResource(R.string.save_as_new_routine)) },
            text = {
                OutlinedTextField(
                    value = newRoutineName,
                    onValueChange = { newRoutineName = it },
                    label = { Text(stringResource(R.string.routine_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.saveAsNewRoutineAndFinish(newRoutineName) },
                    enabled = newRoutineName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNewRoutineNameDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Editing a finished workout: Cancel, the back arrow and the system back all go through here.
    // Nothing changed -> leave straight away; otherwise ask before throwing the edits away.
    var pendingDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }
    val focusManager = LocalFocusManager.current
    fun requestDiscard(afterDiscard: () -> Unit) {
        // A focused set field ignores new values, so it would keep showing the discarded edit.
        focusManager.clearFocus()
        scope.launch {
            if (viewModel.hasUnsavedHistoryChanges()) pendingDiscard = afterDiscard
            else viewModel.cancelHistoryEdit(afterDiscard)
        }
    }
    BackHandler(enabled = state.isEditingHistory) { requestDiscard(onBack) }
    pendingDiscard?.let { afterDiscard ->
        AlertDialog(
            onDismissRequest = { pendingDiscard = null },
            title = { Text(stringResource(R.string.discard_changes_title)) },
            text = { Text(stringResource(R.string.discard_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDiscard = null
                    viewModel.cancelHistoryEdit(afterDiscard)
                }) {
                    Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDiscard = null }) { Text(stringResource(R.string.keep_editing)) }
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (state.isEditingHistory) requestDiscard(onBack) else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                    }
                    Text(
                        text = when {
                            state.isEditingHistory -> stringResource(R.string.edit_workout)
                            state.isViewMode -> stringResource(R.string.workout_details)
                            else -> stringResource(R.string.active_workout)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    when {
                        state.isEditingHistory -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { requestDiscard {} }) {
                                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
                                }
                                Button(onClick = { viewModel.exitHistoryEditMode() }) {
                                    Icon(Icons.Default.Check, null, Modifier.padding(end = 4.dp))
                                    Text(stringResource(R.string.save))
                                }
                            }
                        }
                        state.isViewMode -> {
                            Button(onClick = { viewModel.enterHistoryEditMode() }) {
                                Icon(Icons.Default.Edit, null, Modifier.padding(end = 4.dp))
                                Text(stringResource(R.string.edit))
                            }
                        }
                        else -> {
                            TextButton(onClick = { showCancelDialog = true }) {
                                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
                            }
                            Button(onClick = { viewModel.finishWorkout() }) {
                                Icon(Icons.Default.Check, null, Modifier.padding(end = 4.dp))
                                Text(stringResource(R.string.finish))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (state.isViewMode) {
                    Text(
                        text = state.workoutName ?: "",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    SelectableTextField(
                        value = state.workoutName ?: "",
                        onValueChange = { viewModel.updateWorkoutName(it) },
                        placeholder = stringResource(R.string.workout_name),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                if (state.isEditingHistory) {
                    StartTimeEditor(
                        startTime = state.startTime,
                        onChange = { viewModel.updateWorkoutStartTime(it) }
                    )
                } else if (!state.isViewMode && state.startTime > 0L) {
                    WorkoutTimer(startTime = state.startTime)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if ((state.isViewMode || state.isEditingHistory) && prs.isNotEmpty()) {
                    item(key = "pr-summary") {
                        PrSummaryCard(prs = prs, weightUnit = weightUnit)
                    }
                }
                items(state.workoutExercises, key = { it.workoutExerciseId }) { exercise ->
                    val index = state.workoutExercises.indexOf(exercise)

                    ExerciseBlock(
                        exercise = exercise,
                        weightUnit = weightUnit,
                        index = index,
                        totalCount = state.workoutExercises.size,
                        rpeTrackingEnabled = state.rpeTrackingEnabled,
                        onUpdateSet = { setIndex, weight, reps ->
                            viewModel.updateSet(exercise.workoutExerciseId, setIndex, weight, reps)
                        },
                        onUpdateSetRest = { setIndex, restSec ->
                            viewModel.updateSetRest(exercise.workoutExerciseId, setIndex, restSec)
                        },
                        onUpdateSetRpe = { setIndex, rpe ->
                            viewModel.updateSetRpe(exercise.workoutExerciseId, setIndex, rpe)
                        },
                        onToggleWarmUp = { setIndex ->
                            viewModel.toggleWarmUp(exercise.workoutExerciseId, setIndex)
                        },
                        onLogSet = { setIndex ->
                            viewModel.logSet(exercise.workoutExerciseId, setIndex)
                        },
                        onAddSet = { viewModel.addSet(exercise.workoutExerciseId) },
                        onDeleteSet = { setIndex ->
                            viewModel.deleteSet(exercise.workoutExerciseId, setIndex)
                        },
                        onRemoveExercise = { viewModel.removeExercise(exercise.workoutExerciseId) },
                        onReplaceExercise = { workoutExerciseId ->
                            exercisePickerResultHolder.isReplaceMode = true
                            exercisePickerResultHolder.replacingExerciseId = workoutExerciseId
                            exercisePickerResultHolder.consume()
                            onNavigateToReplacePicker?.invoke()
                        },
                        onMoveUp = {
                            if (index > 0) {
                                viewModel.reorderExercise(index, index - 1)
                                scope.launch { listState.animateScrollToItem(index - 1) }
                            }
                        },
                        onMoveDown = {
                            if (index < state.workoutExercises.size - 1) {
                                viewModel.reorderExercise(index, index + 1)
                                scope.launch { listState.animateScrollToItem(index + 1) }
                            }
                        },
                        onViewExercise = { onViewExerciseDetail(exercise.exerciseId, exercise.workoutExerciseId) },
                        onSaveNote = { noteText -> viewModel.saveExerciseNote(exercise.exerciseId, noteText) },
                        isViewMode = state.isViewMode
                    )
                }

                item {
                    if (!state.isViewMode) {
                        Button(
                            onClick = onAddExercise,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.FitnessCenter, null, Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.add_exercise))
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.isTimerRunning,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                RestTimerBar(
                    secondsRemaining = state.timerRemainingSeconds,
                    totalSeconds = state.timerTotalSeconds,
                    timerAdjustmentSeconds = state.timerAdjustmentSeconds,
                    onSkip = { viewModel.skipRestTimer() },
                    onAdjustTimer = { viewModel.adjustTimer(it) }
                )
            }
        }
    }
}

@Composable
fun SelectableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf(TextFieldValue(text = value)) }

    LaunchedEffect(value) {
        if (!isFocused) {
            textState = TextFieldValue(text = value)
        }
    }

    BasicTextField(
        value = textState,
        onValueChange = { newValue ->
            textState = newValue
            onValueChange(newValue.text)
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
            },
        textStyle = textStyle.copy(
            color = if (isFocused || value.isNotEmpty()) {
                textStyle.color
            } else {
                textStyle.color.copy(alpha = 0.5f)
            }
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = textStyle.color.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun ExerciseBlock(
    exercise: WorkoutExerciseUi,
    weightUnit: WeightUnit,
    index: Int,
    totalCount: Int,
    rpeTrackingEnabled: Boolean = false,
    onUpdateSet: (Int, Float, Int) -> Unit,
    onUpdateSetRest: (Int, Int) -> Unit,
    onUpdateSetRpe: (Int, Float?) -> Unit = { _, _ -> },
    onToggleWarmUp: (Int) -> Unit,
    onLogSet: (Int) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    onRemoveExercise: () -> Unit,
    onReplaceExercise: (Long) -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onViewExercise: () -> Unit = {},
    onSaveNote: (String) -> Unit = {},
    isViewMode: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showPlateCalc by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(exercise.noteText) }
    val isCardio = exercise.muscleGroup == MuscleGroup.CARDIO
    // RPE rates how hard a set of reps was; a cardio block is logged as time, so the column and
    // its after-the-set prompt are dropped there even when RPE tracking is on.
    val showRpe = rpeTrackingEnabled && !isCardio

    if (showPlateCalc) {
        val initial = exercise.sets.firstOrNull { !it.isCompleted && it.weight > 0f }?.weight
            ?: exercise.sets.lastOrNull()?.weight
            ?: 0f
        PlateCalculatorDialog(
            weightUnit = weightUnit,
            initialTargetWeight = kgToDisplay(initial, weightUnit),
            onDismiss = { showPlateCalc = false },
            exerciseId = exercise.exerciseId
        )
    }

    LaunchedEffect(exercise.noteText) {
        noteText = exercise.noteText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!isViewMode) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = index > 0
                        ) {
                            Icon(Icons.Default.ArrowUpward, stringResource(R.string.move_up))
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = index < totalCount - 1
                        ) {
                            Icon(Icons.Default.ArrowDownward, stringResource(R.string.move_down))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onViewExercise() }
                    ) {
                        Text(
                            text = exercise.exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = exerciseSubtitle(exercise),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!isViewMode) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.options))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.replace_exercise)) },
                                onClick = {
                                    showMenu = false
                                    onReplaceExercise(exercise.workoutExerciseId)
                                },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(if (exercise.noteText.isEmpty()) R.string.add_note else R.string.edit_note)) },
                                onClick = {
                                    showMenu = false
                                    showNoteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.plate_calculator)) },
                                onClick = {
                                    showMenu = false
                                    showPlateCalc = true
                                },
                                leadingIcon = { Icon(Icons.Default.FitnessCenter, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remove_exercise)) },
                                onClick = {
                                    showMenu = false
                                    onRemoveExercise()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null) }
                            )
                        }
                    }
                }
            }

            if (showNoteDialog) {
                AlertDialog(
                    onDismissRequest = { showNoteDialog = false },
                    title = { Text(stringResource(R.string.exercise_note)) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text(stringResource(R.string.note)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6
                            )
                            if (exercise.noteText.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        noteText = ""
                                        onSaveNote("")
                                        showNoteDialog = false
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(stringResource(R.string.remove_note), color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onSaveNote(noteText)
                            showNoteDialog = false
                        }) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNoteDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (exercise.noteText.isNotEmpty()) {
                Text(
                    text = exercise.noteText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(stringResource(R.string.col_set), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center)
                Text(stringResource(R.string.col_last), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Text(
                    if (isCardio) stringResource(R.string.col_level) else weightUnitLabel(weightUnit),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    stringResource(if (isCardio) R.string.col_duration else R.string.col_reps),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(stringResource(R.string.col_rest), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                if (showRpe) {
                    Text(stringResource(R.string.col_rpe), style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                }
                Text("", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.3f))
            }

            exercise.sets.forEachIndexed { setIndex, set ->
                // Keyed so a deleted set takes its own row (and swipe state) with it; positional
                // rows shifted state between sets and were tied to a Compose layout crash.
                key(set.uiKey) {
                    SwipeableSetRow(
                        set = set,
                        weightUnit = weightUnit,
                        index = setIndex,
                        rpeTrackingEnabled = showRpe,
                        isCardio = isCardio,
                        onUpdateSet = onUpdateSet,
                        onUpdateRest = { restSec -> onUpdateSetRest(setIndex, restSec) },
                        onUpdateRpe = { rpe -> onUpdateSetRpe(setIndex, rpe) },
                        onToggleWarmUp = { onToggleWarmUp(setIndex) },
                        onLogSet = onLogSet,
                        onDeleteSet = onDeleteSet,
                        isViewMode = isViewMode
                    )
                }
            }

            if (!isViewMode) {
                Button(
                    onClick = onAddSet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.add_set))
                }
            }
        }
    }
}

@Composable
fun RestTimerBar(
    secondsRemaining: Int,
    totalSeconds: Int,
    timerAdjustmentSeconds: Int,
    onSkip: () -> Unit,
    onAdjustTimer: (Int) -> Unit
) {
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.rest_countdown, formatDuration(secondsRemaining)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onAdjustTimer(-timerAdjustmentSeconds) }) {
                    Text("-" + stringResource(R.string.seconds_short, timerAdjustmentSeconds))
                }
                TextButton(onClick = { onAdjustTimer(timerAdjustmentSeconds) }) {
                    Text("+" + stringResource(R.string.seconds_short, timerAdjustmentSeconds))
                }
            }
            TextButton(onClick = onSkip) {
                Icon(Icons.Default.SkipNext, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.skip))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SwipeableSetRow(
    set: SetUi,
    weightUnit: WeightUnit,
    index: Int,
    rpeTrackingEnabled: Boolean = false,
    isCardio: Boolean = false,
    onUpdateSet: (Int, Float, Int) -> Unit,
    onUpdateRest: (Int) -> Unit,
    onUpdateRpe: (Float?) -> Unit = {},
    onToggleWarmUp: () -> Unit,
    onLogSet: (Int) -> Unit,
    onDeleteSet: (Int) -> Unit,
    isViewMode: Boolean = false
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRpePicker by remember { mutableStateOf(false) }

    if (showRpePicker) {
        RpePickerDialog(
            currentRpe = set.rpe,
            onDismiss = { showRpePicker = false },
            onSelect = { value ->
                onUpdateRpe(value)
                showRpePicker = false
            }
        )
    }

    val deleteThreshold = 120f

    val draggableState = rememberDraggableState { delta ->
        if (delta < 0 && !isViewMode) {
            offsetX += delta
            if (offsetX < -deleteThreshold) {
                offsetX = -deleteThreshold
            }
        }
    }

    val historyLabel = set.previousSetInfo?.let {
        val performed = if (isCardio) formatDuration(it.reps) else it.reps.toString()
        "${formatWeightForDisplay(it.weight, weightUnit)}-$performed"
    } ?: "-"

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_completed_set_title)) },
            text = { Text(stringResource(R.string.delete_completed_set_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteSet(index)
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX <= -deleteThreshold && !isViewMode && !set.isCompleted) {
                            // The row is about to be removed: moving it back now would queue a
                            // re-placement for a node that is being detached.
                            onDeleteSet(index)
                        } else {
                            if (offsetX <= -deleteThreshold && !isViewMode) showDeleteConfirm = true
                            offsetX = 0f
                        }
                    }
                )
                .background(
                    when {
                        set.isCompleted -> LocalSuccessColor.current.copy(alpha = 0.3f)
                        set.setType == SetType.WARM_UP -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f)
                    }
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isWarmUp = set.setType == SetType.WARM_UP
            Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
                if (isViewMode) {
                    if (isWarmUp) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            stringResource(R.string.warm_up_set),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onToggleWarmUp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = stringResource(if (isWarmUp) R.string.warm_up_set else R.string.mark_as_warm_up),
                            tint = if (isWarmUp) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.3f),
                textAlign = TextAlign.Center
            )

            Text(
                text = historyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Center
            )

            SetTextField(
                value = kgToDisplay(set.weight, weightUnit),
                onValueChange = { w -> onUpdateSet(index, displayToKg(w, weightUnit), set.reps) },
                modifier = Modifier.weight(1f)
            )

            if (isCardio) {
                DurationTextField(
                    totalSeconds = set.reps,
                    onValueChange = { seconds -> onUpdateSet(index, set.weight, seconds) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                SetTextField(
                    value = set.reps.toFloat(),
                    onValueChange = { r -> onUpdateSet(index, set.weight, r.toInt()) },
                    modifier = Modifier.weight(1f),
                    isInteger = true
                )
            }

            DurationTextField(
                totalSeconds = set.restSeconds,
                onValueChange = { seconds -> onUpdateRest(seconds) },
                modifier = Modifier.weight(0.9f)
            )

            if (rpeTrackingEnabled) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .clickable(enabled = !isViewMode && set.setType != SetType.WARM_UP) { showRpePicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    val rpeText = set.rpe?.let { if (it % 1f == 0f) it.toInt().toString() else String.format(Locale.getDefault(), "%.1f", it) } ?: "—"
                    Text(
                        text = rpeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (set.rpe != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (set.isCompleted || isViewMode) {
                Icon(
                    Icons.Default.Check,
                    stringResource(R.string.completed),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(0.3f)
                )
            } else {
                IconButton(
                    onClick = {
                        onLogSet(index)
                        if (rpeTrackingEnabled && set.rpe == null && set.setType != SetType.WARM_UP) {
                            showRpePicker = true
                        }
                    },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Icon(Icons.Default.Check, stringResource(R.string.log_set))
                }
            }
        }
    }
}

@Composable
fun SetTextField(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isInteger: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(value) {
        if (!isFocused) {
            val current = if (value == 0f) "" else {
                if (isInteger) value.toInt().toString() else formatDecimal(value)
            }
            textState = TextFieldValue(text = current)
        }
    }

    BasicTextField(
        value = textState,
        onValueChange = { newText ->
            textState = newText
            if (isInteger) {
                val filtered = newText.text.filter { c -> c.isDigit() || c == '-' }
                textState = textState.copy(text = filtered, selection = TextRange(filtered.length))
                if (filtered.isEmpty()) {
                    onValueChange(0f)
                } else {
                    filtered.toIntOrNull()?.let { onValueChange(it.toFloat()) }
                }
            } else {
                if (newText.text.isEmpty()) {
                    onValueChange(0f)
                } else {
                    parseDecimalInput(newText.text)?.let { onValueChange(it) }
                }
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .reopenKeyboardOnTap { isFocused }
            .onFocusChanged {
                val lostFocus = isFocused && !it.isFocused
                isFocused = it.isFocused
                if (lostFocus) {
                    // Anything that failed to parse never reached `value`, so snap the text back
                    // to it: what is on screen always matches what will be logged.
                    textState = TextFieldValue(
                        text = if (value == 0f) "" else {
                            if (isInteger) value.toInt().toString() else formatDecimal(value)
                        }
                    )
                }
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center,
            color = if (isFocused || textState.text.isNotEmpty()) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        ),
        keyboardOptions = KeyboardOptions(keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp, vertical = 4.dp)
                    .clickable {
                        if (!isFocused) {
                            focusRequester.requestFocus()
                            val current = textState.text
                            textState = TextFieldValue(
                                text = current,
                                selection = TextRange(0, current.length)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (textState.text.isEmpty() && !isFocused) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun WorkoutFinishedScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.workout_complete),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.workout_complete_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text(stringResource(R.string.back_to_workouts))
        }
    }
}

@Composable
private fun WorkoutTimer(startTime: Long) {
    var elapsedText by remember { mutableStateOf("00:00") }

    LaunchedEffect(startTime) {
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val totalSeconds = (elapsed / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hours = minutes / 60
            elapsedText = if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
            delay(1000)
        }
    }

    Text(
        text = "\u23F1 $elapsedText",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTimeEditor(
    startTime: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val dateFormat = rememberLocalizedDateFormat("MMMdyyyy")
    val timeFormat = java.text.SimpleDateFormat("HH:mm", androidx.compose.ui.platform.LocalConfiguration.current.locales[0])
    val date = java.util.Date(if (startTime > 0L) startTime else System.currentTimeMillis())

    if (showDate) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startTime)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { picked ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
                        val pickedCal = java.util.Calendar.getInstance().apply { timeInMillis = picked }
                        cal.set(java.util.Calendar.YEAR, pickedCal.get(java.util.Calendar.YEAR))
                        cal.set(java.util.Calendar.MONTH, pickedCal.get(java.util.Calendar.MONTH))
                        cal.set(java.util.Calendar.DAY_OF_MONTH, pickedCal.get(java.util.Calendar.DAY_OF_MONTH))
                        onChange(cal.timeInMillis)
                    }
                    showDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTime) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
        val timeState = rememberTimePickerState(
            initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text(stringResource(R.string.pick_start_time)) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val c = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
                    c.set(java.util.Calendar.HOUR_OF_DAY, timeState.hour)
                    c.set(java.util.Calendar.MINUTE, timeState.minute)
                    c.set(java.util.Calendar.SECOND, 0)
                    onChange(c.timeInMillis)
                    showTime = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.started_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = { showDate = true }) {
            Text(dateFormat.format(date))
        }
        TextButton(onClick = { showTime = true }) {
            Text(timeFormat.format(date))
        }
    }
}

@Composable
fun PrSummaryCard(
    prs: List<com.strongest.app.utils.WorkoutPrInfo>,
    weightUnit: com.strongest.app.data.repository.WeightUnit,
    modifier: Modifier = Modifier
) {
    val unitLabel = com.strongest.app.utils.weightUnitLabel(weightUnit)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = pluralStringResource(R.plurals.personal_record_count, prs.size, prs.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            for (pr in prs) {
                val name = pr.exerciseName ?: "?"
                val w = com.strongest.app.utils.formatWeightForDisplay(pr.weightKg ?: 0f, weightUnit)
                val r = pr.reps ?: 0
                val vol = com.strongest.app.utils.formatWeightForDisplay(pr.volumeKg ?: 0f, weightUnit)
                val text = when (pr.kind) {
                    com.strongest.app.utils.PrKind.WEIGHT -> stringResource(R.string.pr_heaviest_set, name, "$w $unitLabel", r)
                    com.strongest.app.utils.PrKind.ONE_RM ->
                        stringResource(
                            R.string.pr_estimated_1rm,
                            name,
                            "${com.strongest.app.utils.formatWeightForDisplay(pr.oneRmKg ?: 0f, weightUnit)} $unitLabel"
                        )
                    com.strongest.app.utils.PrKind.REPS_AT_WEIGHT -> stringResource(R.string.pr_reps_at_weight, name, "$w $unitLabel", r)
                    com.strongest.app.utils.PrKind.MAX_REPS -> stringResource(R.string.pr_max_reps, name, r)
                    com.strongest.app.utils.PrKind.SET_VOLUME -> stringResource(R.string.pr_set_volume, name, "$w $unitLabel", r, "$vol $unitLabel")
                    com.strongest.app.utils.PrKind.EXERCISE_VOLUME -> stringResource(R.string.pr_exercise_volume, name, "$vol $unitLabel")
                    com.strongest.app.utils.PrKind.CARDIO_DURATION ->
                        stringResource(R.string.pr_longest_session, name, com.strongest.app.utils.formatDuration(r))
                    com.strongest.app.utils.PrKind.CARDIO_LEVEL ->
                        stringResource(R.string.pr_highest_level, name, w, com.strongest.app.utils.formatDuration(r))
                    com.strongest.app.utils.PrKind.VOLUME -> stringResource(R.string.pr_workout_volume, "$vol $unitLabel")
                }
                Text(
                    text = "• $text",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
internal fun rpeDescription(value: Int): String = when (value) {
    10 -> stringResource(R.string.rpe_10)
    9 -> stringResource(R.string.rpe_9)
    8 -> stringResource(R.string.rpe_8)
    7 -> stringResource(R.string.rpe_7)
    6 -> stringResource(R.string.rpe_6)
    5 -> stringResource(R.string.rpe_5)
    4 -> stringResource(R.string.rpe_4)
    3 -> stringResource(R.string.rpe_3)
    2 -> stringResource(R.string.rpe_2)
    1 -> stringResource(R.string.rpe_1)
    else -> ""
}

@Composable
fun RpePickerDialog(
    currentRpe: Float?,
    onDismiss: () -> Unit,
    onSelect: (Float?) -> Unit
) {
    val selectedValue = currentRpe?.toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rate_this_set)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.how_hard_did_it_feel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (value in 10 downTo 1) {
                    val isSelected = selectedValue == value
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value.toFloat()) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rpeDescription(value),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        dismissButton = {
            if (currentRpe != null) {
                TextButton(onClick = { onSelect(null) }) {
                    Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
internal fun exerciseSubtitle(exercise: WorkoutExerciseUi): String =
    exerciseSubtitle(exercise.muscleGroup.name, exercise.equipment, exercise.type)

@Composable
internal fun exerciseSubtitle(muscleGroup: String, equipment: Equipment, type: ExerciseType): String {
    val muscle = MuscleGroup.entries.firstOrNull { it.name == muscleGroup }?.label() ?: muscleGroup
    return "$muscle • ${equipment.label()} • ${type.label()}"
}
