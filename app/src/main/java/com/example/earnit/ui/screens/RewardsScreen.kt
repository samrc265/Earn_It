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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun RewardsScreen(viewModel: MainViewModel) {
    val score by viewModel.score.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val rewardPoints = score / 100
    
    var showNoteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        // --- Score Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Score", style = MaterialTheme.typography.labelLarge)
                Text("$score", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Reward Points Available", style = MaterialTheme.typography.labelLarge)
                Text("★ $rewardPoints", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                Text("(1 Point per 100 Score)", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Notes ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reward Log / Notes", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { showNoteDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(notes) { note ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(note.content, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.deleteNote(note) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog) {
        AddNoteDialog(onDismiss = { showNoteDialog = false }, onAdd = { viewModel.addNote(it) })
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Reward / Cheat Meal") },
        text = { OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Details") }) },
        confirmButton = {
            Button(onClick = {
                if (content.isNotEmpty()) {
                    onAdd(content)
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}