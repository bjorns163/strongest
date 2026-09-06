package com.strongest.app.ui.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.strongest.app.data.model.Equipment
import com.strongest.app.data.model.Exercise
import com.strongest.app.data.model.ExerciseType
import com.strongest.app.data.model.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomExerciseDialog(
    onDismiss: () -> Unit,
    onExerciseCreated: (Exercise) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedEquipment by remember { mutableStateOf(Equipment.DUMBBELL) }
    var selectedType by remember { mutableStateOf(ExerciseType.ISOLATION) }
    var instructions by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var muscleExpanded by remember { mutableStateOf(false) }
    var equipmentExpanded by remember { mutableStateOf(false) }

    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Exercise") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Exercise Name *") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name is required") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Muscle group dropdown
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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

                // Equipment dropdown
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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

                Spacer(modifier = Modifier.height(4.dp))

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
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val customId = -System.currentTimeMillis()
                    val exercise = Exercise(
                        id = customId,
                        name = name.trim(),
                        muscleGroup = selectedMuscleGroup,
                        equipment = selectedEquipment,
                        instructions = instructions.trim(),
                        isCustom = true,
                        type = selectedType
                    )
                    onExerciseCreated(exercise)
                },
                enabled = canSave
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
