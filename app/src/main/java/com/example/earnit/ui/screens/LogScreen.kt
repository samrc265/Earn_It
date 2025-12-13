package com.example.earnit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.model.RewardNote
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun LogScreen(viewModel: MainViewModel) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val rewardPoints = score / 100

    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<RewardNote?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

            // Header with Animated Counter
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Spent Rewards", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)

                    // Animated points badge
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        AnimatedContent(
                            targetState = rewardPoints,
                            transitionSpec = {
                                slideInVertically { height -> height } + fadeIn() togetherWith
                                        slideOutVertically { height -> -height } + fadeOut()
                            },
                            label = "PointsAnim"
                        ) { count ->
                            Text(
                                text = "$count pts left",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.weight(1f).animateContentSize()
            ) {
                items(items = notes, key = { it.id }) { note ->
                    Card(
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { noteToEdit = note }
                            .animateItem() // Standard list animation (Compose 1.7+)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                note.content,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { if (rewardPoints > 0) showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = if (rewardPoints > 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (rewardPoints > 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { viewModel.addNote(it) }
        )
    }

    if (noteToEdit != null) {
        EditNoteDialog(
            note = noteToEdit!!,
            onDismiss = { noteToEdit = null },
            onUpdate = { newContent ->
                viewModel.updateNote(noteToEdit!!, newContent)
                noteToEdit = null
            },
            onDelete = {
                viewModel.deleteNote(noteToEdit!!)
                noteToEdit = null
            }
        )
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Spend 1 Point") },
        text = {
            Column {
                Text("What did you spend this point on?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (content.isNotEmpty()) {
                    onAdd(content)
                    onDismiss()
                }
            }) { Text("Spend & Log") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditNoteDialog(
    note: RewardNote,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var content by remember { mutableStateOf(note.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Log") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Edit Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onUpdate(content) }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete (+1 Point)")
            }
        }
    )
}