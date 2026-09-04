package com.strongest.app.ui.exercise

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.OneRmFormula
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.ui.navigation.AddWarmUpSetsRequest
import com.strongest.app.ui.navigation.WarmUpSetSpec
import com.strongest.app.utils.OneRepMaxCalculator
import com.strongest.app.utils.SettingsEntryPoint
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.rememberWeightUnit
import com.strongest.app.utils.weightUnitLabel
import coil3.compose.SubcomposeAsyncImage
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
    workoutExerciseId: Long? = null,
    routineExerciseId: Long? = null,
    onAddWarmUpSets: (AddWarmUpSetsRequest) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    var showEditDialog by remember { mutableStateOf(value = false) }
    var showDeleteConfirm by remember { mutableStateOf(value = false) }

    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                }
                Text(
                    text = "Exercise Details",
                    style = MaterialTheme.typography.titleLarge
                )
                if (state.exercise?.isCustom == true) {
                    Row {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, "Edit exercise")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete exercise",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.exercise != null) {
            ExerciseDetailContent(
                exercise = state.exercise!!,
                history = state.history,
                totalSets = state.totalSets,
                totalWorkouts = state.totalWorkouts,
                maxWeight = state.maxWeight,
                noteText = state.noteText,
                rpeTrackingEnabled = state.rpeTrackingEnabled,
                warmUpSetCount = state.warmUpSetCount,
                onWarmUpSetCountChange = viewModel::setWarmUpSetCount,
                workoutExerciseId = workoutExerciseId,
                routineExerciseId = routineExerciseId,
                onAddWarmUpSets = onAddWarmUpSets,
                onSaveNote = { viewModel.saveNote(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if ((showEditDialog) && state.exercise != null) {
        EditExerciseDialog(
            exercise = state.exercise!!,
            onDismiss = { showEditDialog = false },
        ) { name, muscleGroup, equipment, type, instructions ->
            viewModel.updateExercise(name, muscleGroup, equipment, type, instructions)
            showEditDialog = false
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Exercise") },
            text = { Text("Are you sure you want to delete this custom exercise? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteExercise(onBack)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExerciseDetailContent(
    exercise: com.strongest.app.data.model.Exercise,
    history: List<com.strongest.app.data.db.ExerciseHistoryEntry>,
    totalSets: Int,
    totalWorkouts: Int,
    maxWeight: Float,
    noteText: String,
    rpeTrackingEnabled: Boolean = false,
    modifier: Modifier = Modifier,
    onSaveNote: (String) -> Unit = {},
    warmUpSetCount: Int = 3,
    onWarmUpSetCountChange: (Int) -> Unit = {},
    workoutExerciseId: Long? = null,
    routineExerciseId: Long? = null,
    onAddWarmUpSets: (AddWarmUpSetsRequest) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isCardio = exercise.muscleGroup == com.strongest.app.data.model.MuscleGroup.CARDIO
    val tabs = buildList {
        add("Details" to Icons.Default.Info)
        add("History" to Icons.AutoMirrored.Filled.ShowChart)
        if (!isCardio) {
            add("1RM" to Icons.Default.Calculate)
            add("Warm-up" to Icons.Default.LocalFireDepartment)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = { Icon(imageVector = icon, contentDescription = null) }
                )
            }
        }

        val bestWorkingSet = history
            .filter { it.setType != com.strongest.app.data.model.SetType.WARM_UP }
            .maxByOrNull { it.weightKg }
            ?: history.maxByOrNull { it.weightKg }
        val initialWeightKg = bestWorkingSet?.weightKg ?: maxWeight
        val initialReps = bestWorkingSet?.reps ?: 0

        when (tabs[selectedTab].first) {
            "Details" -> DetailsTab(exercise = exercise, noteText = noteText, onSaveNote = onSaveNote)
            "History" -> HistoryTab(history, totalSets, totalWorkouts, maxWeight, rpeTrackingEnabled, isCardio = isCardio)
            "1RM" -> OneRmTab(initialWeightKg = initialWeightKg, initialReps = initialReps)
            "Warm-up" -> WarmupTab(
                initialWeightKg = initialWeightKg,
                initialReps = initialReps,
                warmUpSetCount = warmUpSetCount,
                onWarmUpSetCountChange = onWarmUpSetCountChange,
                addWarmUpSetsLabel = when {
                    workoutExerciseId != null -> "Add warm-up sets to workout"
                    routineExerciseId != null -> "Add warm-up sets to routine"
                    else -> "Add warm-up sets"
                },
                addWarmUpSetsAction = when {
                    workoutExerciseId != null -> { sets ->
                        onAddWarmUpSets(AddWarmUpSetsRequest(workoutExerciseId = workoutExerciseId, sets = sets))
                    }
                    routineExerciseId != null -> { sets ->
                        onAddWarmUpSets(AddWarmUpSetsRequest(routineExerciseId = routineExerciseId, sets = sets))
                    }
                    else -> null
                }
            )
        }
    }
}

@Composable
fun DetailsTab(
    exercise: com.strongest.app.data.model.Exercise,
    noteText: String,
    onSaveNote: (String) -> Unit
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var editNoteText by remember { mutableStateOf(noteText) }

    LaunchedEffect(noteText) {
        editNoteText = noteText
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ExerciseImage(exercise.id, exercise.name)
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = exercise.muscleGroup.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                    )
                    Text(
                        text = "\u2022",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exercise.equipment.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "\u2022",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exercise.type.label(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (exercise.secondaryMuscles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Also works: " + exercise.secondaryMuscles.joinToString(" • ") { muscle ->
                            muscle.name.lowercase().replaceFirstChar { it.uppercase() }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (exercise.description.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exercise.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (exercise.instructions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How to Perform",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        parseInstructionSteps(exercise.instructions).forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                onClick = { showNoteDialog = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Note",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (noteText.isNotEmpty()) {
                            Text(
                                text = noteText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Text(
                                text = "Tap to add a note...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("My Note") },
            text = {
                OutlinedTextField(
                    value = editNoteText,
                    onValueChange = { editNoteText = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveNote(editNoteText)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseDialog(
    exercise: com.strongest.app.data.model.Exercise,
    onDismiss: () -> Unit,
    onSave: (String, MuscleGroup, Equipment, ExerciseType, String) -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var selectedMuscleGroup by remember { mutableStateOf(exercise.muscleGroup) }
    var selectedEquipment by remember { mutableStateOf(exercise.equipment) }
    var selectedType by remember { mutableStateOf(exercise.type) }
    var instructions by remember { mutableStateOf(exercise.instructions) }
    var muscleExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Exercise") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = muscleExpanded,
                    onExpandedChange = { muscleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedMuscleGroup.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Muscle Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = muscleExpanded,
                        onDismissRequest = { muscleExpanded = false }
                    ) {
                        MuscleGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedMuscleGroup = group
                                    muscleExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = equipmentExpanded,
                    onExpandedChange = { equipmentExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedEquipment.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = equipmentExpanded,
                        onDismissRequest = { equipmentExpanded = false }
                    ) {
                        Equipment.entries.forEach { eq ->
                            DropdownMenuItem(
                                text = { Text(eq.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedEquipment = eq
                                    equipmentExpanded = false
                                }
                            )
                        }
                    }
                }

                ExerciseTypeField(
                    type = selectedType,
                    onTypeChange = { selectedType = it }
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions (optional)") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            selectedMuscleGroup,
                            selectedEquipment,
                            selectedType,
                            instructions.trim()
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Splits an exercise's [Exercise.instructions] text into individual numbered steps.
 *
 * Authoring in the seed data:
 *  - Easiest: write each step as its own sentence ending with a period, e.g.
 *    "Lie on your back on the bench. Hold the weights straight above you. Move them out to your sides."
 *  - For full control (steps that themselves contain periods or commas), separate steps with
 *    a newline (\n) instead. Any leading "1." / "2)" numbering the author types is stripped.
 *
 * Splitting on sentences only breaks after a period followed by whitespace, so decimals like
 * "2.5 kg" stay inside a single step.
 */
private fun parseInstructionSteps(raw: String): List<String> {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return emptyList()
    val rawSteps = if (trimmed.contains('\n')) {
        trimmed.split('\n')
    } else {
        Regex("(?<=\\.)\\s+").split(trimmed)
    }
    return rawSteps
        .map { it.trim().replaceFirst(Regex("^\\d+[.)]\\s*"), "").trim() }
        .filter { it.isNotBlank() }
}

@Composable
fun ExerciseImage(exerciseId: Long, exerciseName: String) {
    ExerciseMovementImage(
        exerciseId = exerciseId,
        contentDescription = exerciseName,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
    HorizontalDivider()
}

@Composable
fun HistoryTab(
    history: List<com.strongest.app.data.db.ExerciseHistoryEntry>,
    totalSets: Int,
    totalWorkouts: Int,
    maxWeight: Float,
    rpeTrackingEnabled: Boolean = false,
    isCardio: Boolean = false,
) {
    val weightUnit by rememberWeightUnit()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    label = "Total Sets",
                    value = totalSets.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Workouts",
                    value = totalWorkouts.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = if (isCardio) "Max Level" else "Max Weight",
                    value = if (isCardio) {
                        formatWeightForDisplay(maxWeight, weightUnit)
                    } else {
                        "${formatWeightForDisplay(maxWeight, weightUnit)} ${weightUnitLabel(weightUnit)}"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No history yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Complete a workout to see history here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(
                history.groupBy { it.workoutDate }
                    .toList()
                    .sortedByDescending { it.first }
            ) { (date, entries) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dateFormat.format(Date(date)),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${entries.size} sets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(4.dp))
                        entries.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (entry.setType == com.strongest.app.data.model.SetType.WARM_UP) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Warm-up set",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isCardio) {
                                            formatWeightForDisplay(entry.weightKg, weightUnit)
                                        } else {
                                            "${formatWeightForDisplay(entry.weightKg, weightUnit)} ${weightUnitLabel(weightUnit)}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = if (isCardio) {
                                        "\u00d7 ${entry.reps}"
                                    } else {
                                        "\u00d7 ${entry.reps} reps"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (rpeTrackingEnabled && entry.rpe != null) {
                                    Text(
                                        text = "RPE ${formatDisplayValue(entry.rpe)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                entry.routineName?.let { routineName ->
                                    Text(
                                        text = routineName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDisplayValue(value: Float): String {
    val rounded = (value * 10f).roundToInt() / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else String.format(Locale.getDefault(), "%.1f", rounded)
}

@Composable
fun OneRmTab(initialWeightKg: Float, initialReps: Int) {
    val weightUnit by rememberWeightUnit()
    val unitLabel = weightUnitLabel(weightUnit)
    val context = LocalContext.current
    val settingsRepo = remember {
        EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java).settingsRepository()
    }
    val appSettings by settingsRepo.settingsFlow.collectAsState(initial = null)
    val formula = appSettings?.oneRmFormula ?: OneRmFormula.EPLEY

    var weightText by remember(weightUnit, initialWeightKg) {
        mutableStateOf(
            if (initialWeightKg > 0f) formatDisplayValue(kgToDisplay(initialWeightKg, weightUnit)) else ""
        )
    }
    var repsText by remember(initialReps) {
        mutableStateOf(if (initialReps > 0) initialReps.toString() else "")
    }

    val weight = weightText.toFloatOrNull() ?: 0f
    val reps = repsText.toIntOrNull() ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Estimated One-Rep Max",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Formula: " + when (formula) {
                OneRmFormula.EPLEY -> "Epley"
                OneRmFormula.BRZYCKI -> "Brzycki"
                OneRmFormula.BOTH -> "Epley + Brzycki (avg)"
            } + " — change in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { input -> weightText = input.filter { it.isDigit() || it == '.' } },
                label = { Text("Weight ($unitLabel)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { input -> repsText = input.filter { it.isDigit() } },
                label = { Text("Reps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (weight <= 0f || reps <= 0) {
            Text(
                text = "Enter weight and reps to see your estimated 1RM.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        val epley = OneRepMaxCalculator.epley(weight, reps)
        val brzycki = OneRepMaxCalculator.brzycki(weight, reps)
        val oneRm = when (formula) {
            OneRmFormula.EPLEY -> epley
            OneRmFormula.BRZYCKI -> brzycki
            OneRmFormula.BOTH -> (epley + brzycki) / 2f
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${formatDisplayValue(oneRm)} $unitLabel",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Estimated 1RM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (formula == OneRmFormula.BOTH) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Epley: ${formatDisplayValue(epley)} $unitLabel  •  Brzycki: ${formatDisplayValue(brzycki)} $unitLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Weight you can do for…",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Estimated max weight for each rep count, based on your 1RM.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        for (targetReps in 1..12) {
            val w = when (formula) {
                OneRmFormula.EPLEY -> oneRm / (1f + targetReps / 30f)
                OneRmFormula.BRZYCKI -> oneRm * (37f - targetReps) / 36f
                OneRmFormula.BOTH -> {
                    val e = oneRm / (1f + targetReps / 30f)
                    val b = oneRm * (37f - targetReps) / 36f
                    (e + b) / 2f
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$targetReps rep${if (targetReps > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatDisplayValue(w)} $unitLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun WarmupTab(
    initialWeightKg: Float,
    initialReps: Int,
    warmUpSetCount: Int = 3,
    onWarmUpSetCountChange: (Int) -> Unit = {},
    addWarmUpSetsAction: ((List<WarmUpSetSpec>) -> Unit)? = null,
    addWarmUpSetsLabel: String = "Add warm-up sets"
) {
    val weightUnit by rememberWeightUnit()
    val unitLabel = weightUnitLabel(weightUnit)
    val useKg = weightUnit == WeightUnit.KG

    var workingText by remember(weightUnit, initialWeightKg) {
        mutableStateOf(
            if (initialWeightKg > 0f) formatDisplayValue(kgToDisplay(initialWeightKg, weightUnit)) else ""
        )
    }
    var repsText by remember(initialReps) {
        mutableStateOf(if (initialReps > 0) initialReps.toString() else "")
    }
    var sliderCount by remember(warmUpSetCount) { mutableIntStateOf(warmUpSetCount.coerceIn(1, 4)) }

    val working = workingText.toFloatOrNull() ?: 0f
    val reps = repsText.toIntOrNull() ?: 0
    val count = sliderCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Warm-up sets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Do all warm-up sets in order before your working set — each builds on the previous one. " +
                "Rest 30–60 seconds between warm-ups, then take your normal rest before the working set. " +
                "Reps drop as the weight climbs because heavier warm-ups are just for priming, not for fatigue.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = workingText,
                onValueChange = { input -> workingText = input.filter { it.isDigit() || it == '.' } },
                label = { Text("Working ($unitLabel)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { input -> repsText = input.filter { it.isDigit() } },
                label = { Text("Reps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Number of warm-up sets",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "1",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = count.toFloat(),
                onValueChange = { sliderCount = it.roundToInt().coerceIn(1, 4) },
                onValueChangeFinished = { onWarmUpSetCountChange(sliderCount) },
                valueRange = 1f..4f,
                steps = 2,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "4",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$count warm-up set${if (count != 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (working <= 0f || reps <= 0) {
            Text(
                text = "Enter your working weight and reps to see warm-up suggestions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        val increment = if (useKg) 2.5f else 5f
        val scheme = listOf(
            0.5f to minOf(8, reps),
            0.7f to minOf(5, reps),
            0.85f to minOf(3, reps),
            0.95f to minOf(2, reps)
        ).take(count)
        val sets = buildList {
            var lastWeight = 0f
            for ((pct, warmReps) in scheme) {
                val raw = working * pct
                val rounded = (raw / increment).roundToInt() * increment
                if (rounded > 0f && rounded < working && rounded > lastWeight + 0.01f) {
                    add(Triple(rounded, warmReps, false))
                    lastWeight = rounded
                }
            }
            add(Triple(working, reps, true))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                sets.forEachIndexed { idx, (w, r, isWorking) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWorking) "Working set" else "Warm-up ${idx + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isWorking) FontWeight.Bold else FontWeight.Normal,
                            color = if (isWorking) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${formatDisplayValue(w)} $unitLabel × $r",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isWorking) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    if (idx < sets.lastIndex) HorizontalDivider()
                }
            }
        }

        if (addWarmUpSetsAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    addWarmUpSetsAction(
                        sets.filter { !it.third }.map { WarmUpSetSpec(it.first, it.second) }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(addWarmUpSetsLabel)
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
