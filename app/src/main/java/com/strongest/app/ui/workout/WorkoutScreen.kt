package com.strongest.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.MuscleGroup
import com.strongest.app.ui.exercise.AddCustomExerciseDialog
import com.strongest.app.ui.exercise.ExerciseRepositoryEntryPoint
import com.strongest.app.ui.exercise.ExerciseThumbnail
import com.strongest.app.ui.exercise.FILTERABLE_EQUIPMENT
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
    onExerciseClick: (Long) -> Unit = {},
    onStartWorkout: () -> Unit = {},
    onResumeWorkout: (Long) -> Unit = {},
    onRoutineSelect: (Long) -> Unit = {},
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val routineGroups by viewModel.routineGroups.collectAsState()
    val usageCounts by viewModel.exerciseUsageCounts.collectAsState()
    val context = LocalContext.current
    var showCreateExerciseDialog by remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        viewModel.refreshOngoingWorkout()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val ongoingId = uiState.ongoingWorkoutId
                    if (ongoingId != null) {
                        onResumeWorkout(ongoingId)
                    } else {
                        onStartWorkout()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.ongoingWorkoutId != null) {
                    Icon(Icons.Default.PlayArrow, "Resume Workout")
                } else {
                    Icon(Icons.Default.Add, "Start Workout")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Strongest",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your workout tracker",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartWorkout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FitnessCenter, null, Modifier.padding(end = 8.dp))
                        Text("Start Empty Workout")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val ongoingId = uiState.ongoingWorkoutId
                    if (ongoingId != null) {
                        Button(
                            onClick = { onResumeWorkout(ongoingId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.padding(end = 8.dp))
                            Text("Resume Workout")
                        }
                    }
                }
            }

            if (routines.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Routines",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                val groupsById = routineGroups.associateBy { it.id }
                val grouped = linkedMapOf<Long?, MutableList<com.strongest.app.data.model.Routine>>()
                for (g in routineGroups) {
                    grouped[g.id] = mutableListOf()
                }
                for (r in routines) {
                    val key = if ((r.groupId != null) && groupsById.containsKey(r.groupId)) r.groupId else null
                    grouped.getOrPut(key) { mutableListOf() }.add(r)
                }

                for (g in routineGroups) {
                    val list = grouped[g.id].orEmpty()
                    if (list.isEmpty()) continue
                    item(key = "wg-header-${g.id}") {
                        Text(
                            text = g.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(list.size, key = { "wg-${g.id}-${list[it].id}" }) { idx ->
                        val routine = list[idx]
                        RoutineCard(routine = routine, onClick = { onRoutineSelect(routine.id) })
                    }
                }
                val ungrouped = grouped[null].orEmpty()
                if (ungrouped.isNotEmpty()) {
                    if (routineGroups.isNotEmpty()) {
                        item(key = "wg-header-ungrouped") {
                            Text(
                                text = "Ungrouped",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                    items(ungrouped.size, key = { "wg-ungrouped-${ungrouped[it].id}" }) { idx ->
                        val routine = ungrouped[idx]
                        RoutineCard(routine = routine, onClick = { onRoutineSelect(routine.id) })
                    }
                }
            }

            item {
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedMuscleGroup == null,
                            onClick = { viewModel.setMuscleGroup(null) },
                            label = { Text("All") }
                        )
                    }
                    val filterableGroups = MuscleGroup.entries.filter {
                        it != MuscleGroup.OTHER
                    }
                    items(filterableGroups.size) { index ->
                        val group = filterableGroups[index]
                        FilterChip(
                            selected = uiState.selectedMuscleGroup == group,
                            onClick = { viewModel.setMuscleGroup(group) },
                            label = { Text(group.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedEquipment == null,
                            onClick = { viewModel.setEquipment(null) },
                            label = { Text("All") }
                        )
                    }
                    items(FILTERABLE_EQUIPMENT.size) { index ->
                        val eq = FILTERABLE_EQUIPMENT[index]
                        FilterChip(
                            selected = uiState.selectedEquipment == eq,
                            onClick = { viewModel.setEquipment(eq) },
                            label = { Text(eq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            item {
                var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        viewModel.setSearchQuery(query)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search exercises...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
            }

            item {
                TextButton(
                    onClick = { showCreateExerciseDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.padding(end = 4.dp))
                    Text("Create Custom Exercise")
                }
            }

            val filteredExercises = exercises.filter { exercise ->
                (uiState.selectedMuscleGroup == null || exercise.muscleGroup == uiState.selectedMuscleGroup) &&
                (uiState.selectedEquipment == null || exercise.equipment == uiState.selectedEquipment) &&
                (uiState.searchQuery.isEmpty() || exercise.name.contains(uiState.searchQuery, ignoreCase = true))
            }.take(50)

            items(filteredExercises.size) { index ->
                val exercise = filteredExercises[index]
                ExerciseListCard(
                    exercise = exercise,
                    usageCount = usageCounts[exercise.id] ?: 0,
                    onClick = { onExerciseClick(exercise.id) }
                )
            }
        }
    }

    if (showCreateExerciseDialog) {
        AddCustomExerciseDialog(
            onDismiss = { showCreateExerciseDialog = false },
            onExerciseCreated = { exercise ->
                showCreateExerciseDialog = false
                val repo = EntryPointAccessors.fromApplication(
                    context,
                    ExerciseRepositoryEntryPoint::class.java
                ).workoutRepository()
                MainScope().launch {
                    repo.insertCustomExercise(exercise)
                }
            }
        )
    }
}

@Composable
fun RoutineCard(
    routine: com.strongest.app.data.model.Routine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = routine.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (routine.description.isNotEmpty()) {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ExerciseListCard(
    exercise: com.strongest.app.data.model.Exercise,
    usageCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseThumbnail(
                exerciseId = exercise.id,
                contentDescription = exercise.name,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${exercise.muscleGroup.name.lowercase().replaceFirstChar { it.uppercase() }} \u2022 ${exercise.equipment.name.lowercase().replaceFirstChar { it.uppercase() }} \u2022 ${exercise.type.label()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (usageCount > 0) {
                Text(
                    text = usageCount.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Add, "Add to workout")
            }
        }
    }
}
