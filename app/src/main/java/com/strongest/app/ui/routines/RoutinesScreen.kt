package com.strongest.app.ui.routines

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.strongest.app.R
import com.strongest.app.data.model.Routine
import com.strongest.app.data.model.RoutineGroup
import kotlinx.coroutines.launch

@Composable
fun RoutinesScreen(
    onCreateNew: () -> Unit = {},
    onRoutineClick: (Long) -> Unit = {},
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val routines by viewModel.routines.collectAsState()
    val groups by viewModel.routineGroups.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showGroupsDialog by remember { mutableStateOf(false) }
    val importedMessage = stringResource(R.string.routine_imported)
    val importFailedMessage = stringResource(R.string.routine_import_failed)
    val ungroupedTitle = stringResource(R.string.ungrouped)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val jsonString = inputStream?.bufferedReader()?.readText() ?: ""
                    inputStream?.close()
                    viewModel.importRoutine(jsonString)
                    snackbarHostState.showSnackbar(importedMessage)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(importFailedMessage)
                }
            }
        }
    }

    if (showGroupsDialog) {
        ManageGroupsDialog(
            groups = groups,
            onDismiss = { showGroupsDialog = false },
            onCreate = { name -> viewModel.createGroup(name) },
            onRename = { id, name -> viewModel.renameGroup(id, name) },
            onDelete = { id -> viewModel.deleteGroup(id) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNew) {
                Icon(Icons.Default.Add, stringResource(R.string.create_routine))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.nav_routines),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = { showGroupsDialog = true }) {
                        Icon(Icons.Default.Folder, stringResource(R.string.manage_groups))
                    }
                }
            }
            item {
                Button(
                    onClick = onCreateNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.create_routine))
                }
            }
            item {
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.FileDownload, null, Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.import_routine))
                }
            }

            if (routines.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_routines_yet),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(32.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_routines_yet_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val sections = buildRoutineSections(routines, groups, ungroupedTitle)
                sections.forEach { section ->
                    if (section.title != null) {
                        item(key = "header-${section.key}") {
                            SectionHeader(text = section.title)
                        }
                    }
                    items(section.routines.size, key = { "${section.key}-${section.routines[it].id}" }) { idx ->
                        val routine = section.routines[idx]
                        RoutineCard(
                            routine = routine,
                            onClick = { onRoutineClick(routine.id) },
                            onShare = { viewModel.shareRoutine(routine, context) },
                            onDelete = { viewModel.deleteRoutine(routine) }
                        )
                    }
                }
            }
        }
    }
}

private data class RoutineSection(
    val key: String,
    val title: String?,
    val routines: List<Routine>
)

private fun buildRoutineSections(
    routines: List<Routine>,
    groups: List<RoutineGroup>,
    ungroupedTitle: String
): List<RoutineSection> {
    if (groups.isEmpty()) {
        return listOf(RoutineSection(key = "all", title = null, routines = routines))
    }
    val groupsById = groups.associateBy { it.id }
    val grouped = mutableMapOf<Long, MutableList<Routine>>()
    val ungrouped = mutableListOf<Routine>()
    for (r in routines) {
        val gid = r.groupId
        if (gid != null && groupsById.containsKey(gid)) {
            grouped.getOrPut(gid) { mutableListOf() }.add(r)
        } else {
            ungrouped.add(r)
        }
    }
    val result = mutableListOf<RoutineSection>()
    for (g in groups) {
        result.add(
            RoutineSection(
                key = "g-${g.id}",
                title = g.name,
                routines = grouped[g.id] ?: emptyList()
            )
        )
    }
    if (ungrouped.isNotEmpty()) {
        result.add(RoutineSection(key = "ungrouped", title = ungroupedTitle, routines = ungrouped))
    }
    return result
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ManageGroupsDialog(
    groups: List<RoutineGroup>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var newGroupName by remember { mutableStateOf("") }
    var renamingId by remember { mutableStateOf<Long?>(null) }
    var renameText by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<RoutineGroup?>(null) }

    if (pendingDelete != null) {
        val target = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_group_title)) },
            text = { Text(stringResource(R.string.delete_group_message, target.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target.id)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routine_groups)) },
        text = {
            Column {
                if (groups.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_groups_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    groups.forEach { group ->
                        if (renamingId == group.id) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = renameText,
                                    onValueChange = { renameText = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                TextButton(onClick = {
                                    onRename(group.id, renameText)
                                    renamingId = null
                                }) { Text(stringResource(R.string.save)) }
                                TextButton(onClick = { renamingId = null }) { Text(stringResource(R.string.cancel)) }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    renamingId = group.id
                                    renameText = group.name
                                }) {
                                    Icon(Icons.Default.Edit, stringResource(R.string.rename))
                                }
                                IconButton(onClick = { pendingDelete = group }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        stringResource(R.string.delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.new_group_name)) },
                        singleLine = true
                    )
                    TextButton(
                        onClick = {
                            onCreate(newGroupName)
                            newGroupName = ""
                        },
                        enabled = newGroupName.isNotBlank()
                    ) { Text(stringResource(R.string.add)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
fun RoutineCard(
    routine: Routine,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_routine_title)) },
            text = { Text(stringResource(R.string.delete_routine_message, routine.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, stringResource(R.string.share_routine))
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, stringResource(R.string.delete_routine))
            }
        }
    }
}
