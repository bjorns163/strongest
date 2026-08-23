package com.strongest.app.ui.routines

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.data.model.RoutineGroup
import com.strongest.app.data.model.SetType
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.ui.exercise.ExercisePickerResultHolder
import com.strongest.app.utils.displayToKg
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.rememberWeightUnit
import com.strongest.app.utils.weightUnitLabel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RoutineBuilderEntryPoint {
    fun exercisePickerResultHolder(): ExercisePickerResultHolder
}

@Composable
fun RoutineBuilderScreen(
    onBack: () -> Unit,
    onAddExercise: () -> Unit,
    onNavigateToReplacePicker: (() -> Unit)? = null,
    onViewExerciseDetail: (exerciseId: Long, routineExerciseId: Long) -> Unit = { _, _ -> },
    routineId: Long? = null,
    viewModel: RoutineBuilderViewModel = hiltViewModel(),
    warmUpSetsToAdd: com.strongest.app.ui.navigation.AddWarmUpSetsRequest? = null,
    onWarmUpSetsConsumed: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val weightUnit by rememberWeightUnit()

    LaunchedEffect(routineId) {
        if (routineId != null) {
            viewModel.loadRoutine(routineId)
        }
    }

    LaunchedEffect(warmUpSetsToAdd) {
        val request = warmUpSetsToAdd ?: return@LaunchedEffect
        val routineExerciseId = request.routineExerciseId
        if (routineExerciseId != null && request.sets.isNotEmpty()) {
            viewModel.addWarmUpSets(routineExerciseId, request.sets)
        }
        onWarmUpSetsConsumed()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val exercisePickerResultHolder = remember {
        EntryPointAccessors.fromApplication(
            context,
            RoutineBuilderEntryPoint::class.java
        ).exercisePickerResultHolder()
    }
    val pickerResult by exercisePickerResultHolder.result.collectAsState()
    LaunchedEffect(pickerResult) {
        val exerciseIds = pickerResult?.takeIf { it.isNotEmpty() } ?: return@LaunchedEffect
        val replaceTarget = exercisePickerResultHolder.replacingExerciseId
            ?.takeIf { exercisePickerResultHolder.isReplaceMode }
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                    Text(
                        text = if (routineId != null) "Edit Routine" else "New Routine",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row {
                        TextButton(onClick = onBack) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Button(
                            onClick = { viewModel.saveRoutine(); onBack() },
                            enabled = state.routineName.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.padding(end = 4.dp))
                            Text("Save")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.routineName,
                    onValueChange = { viewModel.updateName(it) },
                    placeholder = { Text("Routine name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Normal
                    )
                )
                OutlinedTextField(
                    value = state.routineDescription,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("Description (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                GroupSelector(
                    groups = groups,
                    selectedGroupId = state.groupId,
                    onSelect = { viewModel.updateGroup(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
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
                items(state.exercises, key = { it.routineExerciseId }) { exercise ->
                    val index = state.exercises.indexOf(exercise)

                    RoutineExerciseBlock(
                        exercise = exercise,
                        weightUnit = weightUnit,
                        index = index,
                        totalCount = state.exercises.size,
                        onUpdateSet = { setIndex, weight, reps ->
                            viewModel.updateSet(exercise.routineExerciseId, setIndex, weight, reps)
                        },
                        onUpdateSetRest = { setIndex, restSec ->
                            viewModel.updateSetRest(exercise.routineExerciseId, setIndex, restSec)
                        },
                        onToggleWarmUp = { setIndex ->
                            viewModel.toggleWarmUp(exercise.routineExerciseId, setIndex)
                        },
                        onAddSet = { viewModel.addSet(exercise.routineExerciseId) },
                        onDeleteSet = { setIndex ->
                            viewModel.deleteSet(exercise.routineExerciseId, setIndex)
                        },
                        onRemoveExercise = { viewModel.removeExercise(exercise.routineExerciseId) },
                        onReplaceExercise = { routineExerciseId ->
                            exercisePickerResultHolder.isReplaceMode = true
                            exercisePickerResultHolder.replacingExerciseId = routineExerciseId
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
                            if (index < state.exercises.size - 1) {
                                viewModel.reorderExercise(index, index + 1)
                                scope.launch { listState.animateScrollToItem(index + 1) }
                            }
                        },
                        onViewExercise = { onViewExerciseDetail(exercise.exerciseId, exercise.routineExerciseId) },
                        onSaveNote = { noteText -> viewModel.saveExerciseNote(exercise.exerciseId, noteText) }
                    )
                }

                item {
                    Button(
                        onClick = onAddExercise,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, Modifier.padding(end = 8.dp))
                        Text("Add Exercise")
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineExerciseBlock(
    exercise: RoutineExerciseUi,
    weightUnit: WeightUnit,
    index: Int,
    totalCount: Int,
    onUpdateSet: (Int, Float, Int) -> Unit,
    onUpdateSetRest: (Int, Int) -> Unit,
    onToggleWarmUp: (Int) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    onRemoveExercise: () -> Unit,
    onReplaceExercise: (Long) -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onViewExercise: () -> Unit = {},
    onSaveNote: (String) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(exercise.noteText) }
    val isCardio = exercise.muscleGroup == "CARDIO"

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
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0
                    ) {
                        Icon(Icons.Default.ArrowUpward, "Move up")
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1
                    ) {
                        Icon(Icons.Default.ArrowDownward, "Move down")
                    }
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onViewExercise() }
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Replace Exercise") },
                            onClick = {
                                showMenu = false
                                onReplaceExercise(exercise.routineExerciseId)
                            },
                            leadingIcon = { Icon(Icons.Default.SwapHoriz, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (exercise.noteText.isEmpty()) "Add Note" else "Edit Note") },
                            onClick = {
                                showMenu = false
                                showNoteDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Exercise") },
                            onClick = {
                                showMenu = false
                                onRemoveExercise()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            }

            if (showNoteDialog) {
                AlertDialog(
                    onDismissRequest = { showNoteDialog = false },
                    title = { Text("Exercise Note") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Note") },
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
                                    Text("Remove Note", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onSaveNote(noteText)
                            showNoteDialog = false
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNoteDialog = false }) {
                            Text("Cancel")
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
                Text("Set", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center)
                Text("Last", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                Text(
                    if (isCardio) "Level" else weightUnitLabel(weightUnit),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    if (isCardio) "Duration" else "Reps",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text("Rest", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                Text("", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.3f))
            }

            exercise.sets.forEachIndexed { setIndex, set ->
                SwipeableRoutineSetRow(
                    set = set,
                    weightUnit = weightUnit,
                    index = setIndex,
                    isCardio = isCardio,
                    onUpdateSet = onUpdateSet,
                    onUpdateRest = { restSec -> onUpdateSetRest(setIndex, restSec) },
                    onToggleWarmUp = { onToggleWarmUp(setIndex) },
                    onDeleteSet = onDeleteSet
                )
            }

            Button(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, Modifier.padding(end = 4.dp))
                Text("Add Set")
            }
        }
    }
}

@Composable
fun SwipeableRoutineSetRow(
    set: RoutineSetUi,
    weightUnit: WeightUnit,
    index: Int,
    isCardio: Boolean = false,
    onUpdateSet: (Int, Float, Int) -> Unit,
    onUpdateRest: (Int) -> Unit,
    onToggleWarmUp: () -> Unit,
    onDeleteSet: (Int) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val deleteThreshold = 120f

    val draggableState = rememberDraggableState { delta ->
        if (delta < 0) {
            offsetX += delta
            if (offsetX < -deleteThreshold) {
                offsetX = -deleteThreshold
            }
        }
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
                        if (offsetX <= -deleteThreshold) {
                            onDeleteSet(index)
                        }
                        offsetX = 0f
                    }
                )
                .background(
                    if (set.setType == SetType.WARM_UP) {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f)
                    }
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isWarmUp = set.setType == SetType.WARM_UP
            Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onToggleWarmUp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = if (isWarmUp) "Warm-up set" else "Mark as warm-up set",
                        tint = if (isWarmUp) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.3f),
                textAlign = TextAlign.Center
            )

            val previousLabel = set.previousSetInfo?.let {
                "${formatWeightForDisplay(it.weight, weightUnit)}-${it.reps}"
            } ?: "-"
            Text(
                text = previousLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(0.7f),
                textAlign = TextAlign.Center
            )

            RoutineSetTextField(
                value = kgToDisplay(set.weight, weightUnit),
                onValueChange = { w -> onUpdateSet(index, displayToKg(w, weightUnit), set.reps) },
                modifier = Modifier.weight(1f)
            )

            RoutineSetTextField(
                value = set.reps.toFloat(),
                onValueChange = { r -> onUpdateSet(index, set.weight, r.toInt()) },
                modifier = Modifier.weight(1f),
                isInteger = true
            )

            RoutineSetTextField(
                value = set.restSeconds.toFloat(),
                onValueChange = { s -> onUpdateRest(s.toInt()) },
                modifier = Modifier.weight(0.7f),
                isInteger = true
            )

            Icon(
                Icons.Default.Check,
                null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.weight(0.3f)
            )
        }
    }
}

@Composable
fun RoutineSetTextField(
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
                if (isInteger || value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)
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
                    newText.text.toFloatOrNull()?.let { onValueChange(it) }
                }
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
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
fun GroupSelector(
    groups: List<RoutineGroup>,
    selectedGroupId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = groups.find { it.id == selectedGroupId }?.name ?: "Ungrouped"

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Group",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(Icons.Default.ArrowDropDown, "Select group")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Ungrouped") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = {
                        onSelect(group.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
