package com.strongest.app.ui.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.data.repository.WorkoutRepository
import com.strongest.app.ui.workout.WorkoutViewModel
import kotlinx.coroutines.launch
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ExercisePickerResultHolderEntryPoint {
    fun exercisePickerResultHolder(): ExercisePickerResultHolder
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ExerciseRepositoryEntryPoint {
    fun workoutRepository(): WorkoutRepository
}

val FILTERABLE_MUSCLE_GROUPS = MuscleGroup.entries.filter {
    it != MuscleGroup.OTHER
}

val FILTERABLE_EQUIPMENT = listOf(
    Equipment.BARBELL,
    Equipment.DUMBBELL,
    Equipment.MACHINE,
    Equipment.CABLE,
    Equipment.BODYWEIGHT,
    Equipment.KETTLEBELL,
    Equipment.RESISTANCE_BAND,
    Equipment.MEDICINE_BALL,
    Equipment.SUSPENSION,
    Equipment.SMITH_MACHINE,
    Equipment.EZ_BAR,
    Equipment.TRAP_BAR,
    Equipment.PLATE,
)

@Composable
fun ExercisePickerScreen(
    onExercisesSelected: (List<Long>) -> Unit,
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val exercises by viewModel.exercises.collectAsState()
    val usageCounts by viewModel.exerciseUsageCounts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    val selectedExerciseIds = remember { mutableStateOf(emptySet<Long>()) }
    var showCreateDialog by remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val holder = remember {
        EntryPointAccessors.fromApplication(
            context,
            ExercisePickerResultHolderEntryPoint::class.java
        ).exercisePickerResultHolder()
    }
    // Snapshot once at entry; the host clears these via clearReplaceMode() after consuming the result.
    val isReplaceMode = remember { holder.isReplaceMode }
    val title = if (isReplaceMode) "Replace Exercise" else "Add Exercise"

    val filteredExercises = exercises.filter { exercise ->
        val matchesSearch = searchQuery.isEmpty() || exercise.name.contains(searchQuery, ignoreCase = true)
        val matchesMuscle = selectedMuscleGroup == null || exercise.muscleGroup == selectedMuscleGroup
        val matchesEquipment = selectedEquipment == null || exercise.equipment == selectedEquipment
        matchesSearch && matchesMuscle && matchesEquipment
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
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
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (selectedExerciseIds.value.isNotEmpty()) {
                        val buttonText = if (isReplaceMode) "Replace" else "Add (${selectedExerciseIds.value.size})"
                        Button(
                            onClick = {
                                holder.publish(selectedExerciseIds.value.toList())
                                onExercisesSelected(selectedExerciseIds.value.toList())
                            },
                            enabled = selectedExerciseIds.value.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Add, null, Modifier.padding(end = 4.dp))
                            Text(buttonText)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            OutlinedButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, null, Modifier.padding(end = 4.dp))
                Text("Create Custom Exercise")
            }

            Text(
                text = "Muscle Group",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedMuscleGroup == null,
                        onClick = { selectedMuscleGroup = null },
                        label = { Text("All") }
                    )
                }
                items(FILTERABLE_MUSCLE_GROUPS) { group ->
                    FilterChip(
                        selected = selectedMuscleGroup == group,
                        onClick = { selectedMuscleGroup = group },
                        label = { Text(group.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Text(
                text = "Equipment",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedEquipment == null,
                        onClick = { selectedEquipment = null },
                        label = { Text("All") }
                    )
                }
                items(FILTERABLE_EQUIPMENT) { eq ->
                    FilterChip(
                        selected = selectedEquipment == eq,
                        onClick = { selectedEquipment = eq },
                        label = { Text(eq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredExercises.isEmpty()) {
                    item {
                        Text(
                            text = "No exercises found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    items(filteredExercises) { exercise ->
                        val isSelected = selectedExerciseIds.value.contains(exercise.id)
                        ExercisePickerCard(
                            exercise = exercise,
                            isSelected = isSelected,
                            usageCount = usageCounts[exercise.id] ?: 0,
                            onToggle = {
                                if (isReplaceMode) {
                                    selectedExerciseIds.value = if (isSelected) emptySet() else setOf(exercise.id)
                                } else {
                                    if (isSelected) {
                                        selectedExerciseIds.value -= exercise.id
                                    } else {
                                        selectedExerciseIds.value += exercise.id
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AddCustomExerciseDialog(
            onDismiss = { showCreateDialog = false },
            onExerciseCreated = { exercise ->
                showCreateDialog = false
                scope.launch {
                    val repo = EntryPointAccessors.fromApplication(
                        context,
                        ExerciseRepositoryEntryPoint::class.java
                    ).workoutRepository()
                    repo.insertCustomExercise(exercise)
                }
            }
        )
    }
}

@Composable
fun ExercisePickerCard(
    exercise: Exercise,
    isSelected: Boolean,
    usageCount: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            ExerciseThumbnail(
                exerciseId = exercise.id,
                contentDescription = exercise.name,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.muscleGroup.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "\u2022",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exercise.equipment.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "\u2022",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exercise.classification.label(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (usageCount > 0) {
                Text(
                    text = usageCount.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
