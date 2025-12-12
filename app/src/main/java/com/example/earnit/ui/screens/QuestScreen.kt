package com.example.earnit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.model.Task
import com.example.earnit.model.TaskType
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun QuestScreen(viewModel: MainViewModel) {
    // Observe database changes
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SectionHeader("Daily Quests (2pts)") }
            items(tasks.filter { it.type == TaskType.DAILY }) { task ->
                TaskItem(task, onToggle = { viewModel.toggleTask(task) }, onDelete = { viewModel.deleteTask(task) })
            }

            item { SectionHeader("Short Term (10pts)") }
            items(tasks.filter { it.type == TaskType.SHORT_TERM }) { task ->
                TaskItem(task, onToggle = { viewModel.toggleTask(task) }, onDelete = { viewModel.deleteTask(task) })
            }

            item { SectionHeader("Long Term (50pts)") }
            items(tasks.filter { it.type == TaskType.LONG_TERM }) { task ->
                TaskItem(task, onToggle = { viewModel.toggleTask(task) }, onDelete = { viewModel.deleteTask(task) })
            }
            // Space for the Floating Action Button
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, type -> viewModel.addTask(name, type) }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() })
            Text(
                text = task.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, TaskType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.DAILY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Quest") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Task Name") })
                Spacer(modifier = Modifier.height(8.dp))
                Text("Type:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TaskType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.take(4)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotEmpty()) {
                    onAdd(name, selectedType)
                    onDismiss()
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}