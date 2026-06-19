package com.strongest.app.ui.history

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.strongest.app.data.model.Workout
import com.strongest.app.data.repository.WeightUnit
import com.strongest.app.utils.formatWeightForDisplay
import com.strongest.app.utils.kgToDisplay
import com.strongest.app.utils.rememberWeightUnit
import com.strongest.app.utils.weightUnitLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onWorkoutClick: (Long) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsState()
    val summaries by viewModel.summaries.collectAsState()
    val routineInfo by viewModel.routineInfo.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val completedWorkouts = workouts.filter { it.isOngoing == false }
    val context = LocalContext.current
    val weightUnit by rememberWeightUnit()

    Scaffold(
        floatingActionButton = {
            if (completedWorkouts.isNotEmpty()) {
                if (selectionMode) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                viewModel.shareSelectedWorkouts(context, weightUnit)
                            }
                        },
                        containerColor = if (selectedIds.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.padding(end = 8.dp))
                        Text("Share (${selectedIds.size})")
                    }
                } else {
                    FloatingActionButton(
                        onClick = { viewModel.enterSelectionMode() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Share, "Share workouts")
                    }
                }
            }
        }
    ) { padding ->
        if (completedWorkouts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Workouts Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(32.dp)
                )
                Text(
                    text = "Complete your first workout to see it here",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (selectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.exitSelectionMode() }) {
                                Icon(Icons.Default.Close, "Exit selection")
                            }
                            Text(
                                text = "${selectedIds.size} selected",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        val allSelected = selectedIds.size == completedWorkouts.size && completedWorkouts.isNotEmpty()
                        TextButton(
                            onClick = {
                                if (allSelected) viewModel.clearSelection() else viewModel.selectAll()
                            }
                        ) {
                            Text(if (allSelected) "Clear All" else "Select All")
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(completedWorkouts.size) { index ->
                        val workout = completedWorkouts[index]
                        WorkoutHistoryCard(
                            workout = workout,
                            summary = summaries[workout.id],
                            routineInfo = routineInfo[workout.id],
                            weightUnit = weightUnit,
                            selectionMode = selectionMode,
                            isSelected = workout.id in selectedIds,
                            onClick = {
                                if (selectionMode) viewModel.toggleSelection(workout.id)
                                else onWorkoutClick(workout.id)
                            },
                            onDelete = { viewModel.deleteWorkout(workout.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryCard(
    workout: Workout,
    summary: WorkoutSummary?,
    routineInfo: RoutineInfo?,
    weightUnit: WeightUnit,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Workout") },
            text = { Text("Are you sure you want to delete this workout? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selectionMode && isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.workoutName ?: workout.routineName ?: "Empty Workout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Started at ${timeFormat.format(Date(workout.startTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(Date(workout.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    routineInfo?.groupName?.let { groupName ->
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    val routineDescription = routineInfo?.description
                    if (!routineDescription.isNullOrBlank()) {
                        Text(
                            text = routineDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!selectionMode) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete workout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            val exercises = summary?.exercises.orEmpty()
            if (exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ExerciseSummaryTable(exercises = exercises, weightUnit = weightUnit)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            WorkoutStatsRow(
                workout = workout,
                summary = summary,
                weightUnit = weightUnit
            )
        }
    }
}

@Composable
private fun ExerciseSummaryTable(
    exercises: List<HistoryExerciseSummary>,
    weightUnit: WeightUnit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sets",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Best set",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(4.dp))
    for (ex in exercises) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${ex.setCount}x ${ex.exerciseName}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (ex.setCount > 0 && (ex.bestWeightKg > 0f || ex.bestReps > 0)) {
                    "${formatWeightForDisplay(ex.bestWeightKg, weightUnit)} ${weightUnitLabel(weightUnit)} × ${ex.bestReps}"
                } else {
                    "—"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WorkoutStatsRow(
    workout: Workout,
    summary: WorkoutSummary?,
    weightUnit: WeightUnit
) {
    val durationMin = if (workout.endTime != null) (workout.endTime - workout.startTime) / 60000 else 0L
    val hours = durationMin / 60
    val minutes = durationMin % 60
    val durationText = if (hours > 0) String.format("%d:%02d", hours, minutes) else "${minutes}m"

    val totalVolume = summary?.totalVolumeKg ?: 0f
    val prCount = summary?.prs?.size ?: 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatChip(
            icon = Icons.Default.AccessTime,
            text = durationText,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Default.FitnessCenter,
            text = if (totalVolume > 0) {
                "${formatWeightForDisplay(totalVolume, weightUnit)} ${weightUnitLabel(weightUnit)}"
            } else "—",
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Default.EmojiEvents,
            text = if (prCount > 0) "$prCount PR${if (prCount > 1) "s" else ""}" else "0 PRs",
            highlight = prCount > 0,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
