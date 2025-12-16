package com.example.earnit.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.R
import com.example.earnit.ui.theme.*
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showRestoreDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // --- Color Palette ---
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThemeCircle(color = Purple40, isSelected = themeIndex == 0, onClick = { viewModel.setTheme(0) }, label = "Classic")
            ThemeCircle(color = OceanPrimary, isSelected = themeIndex == 1, onClick = { viewModel.setTheme(1) }, label = "Ocean")
            ThemeCircle(color = NaturePrimary, isSelected = themeIndex == 2, onClick = { viewModel.setTheme(2) }, label = "Nature")
            ThemeCircle(color = SunsetPrimary, isSelected = themeIndex == 3, onClick = { viewModel.setTheme(3) }, label = "Sunset")
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // --- Dark Mode ---
        Text(stringResource(R.string.settings_night_mode), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(selected = darkMode == 0, onClick = { viewModel.setDarkMode(0) }, label = { Text("System") })
            FilterChip(selected = darkMode == 1, onClick = { viewModel.setDarkMode(1) }, label = { Text("Light") })
            FilterChip(selected = darkMode == 2, onClick = { viewModel.setDarkMode(2) }, label = { Text("Dark") })
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // --- Data & General ---
        Text(stringResource(R.string.settings_general), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Backup Button
            OutlinedButton(onClick = {
                val json = viewModel.createBackupJson()
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, json)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Save Backup")
                context.startActivity(shareIntent)
            }) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_backup))
            }

            // Restore Button
            OutlinedButton(onClick = { showRestoreDialog = true }) {
                Text(stringResource(R.string.settings_restore))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = { viewModel.resetOnboarding() },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.settings_tutorial))
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.settings_version), style = MaterialTheme.typography.bodySmall)
                Text("Gamify your life, one task at a time.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Data") },
            text = {
                Column {
                    Text("Paste your backup code here. Warning: This will overwrite current data!")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (viewModel.restoreBackupJson(jsonInput)) {
                        Toast.makeText(context, R.string.restore_success, Toast.LENGTH_SHORT).show()
                        showRestoreDialog = false
                    } else {
                        Toast.makeText(context, R.string.restore_error, Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ThemeCircle(color: Color, isSelected: Boolean, onClick: () -> Unit, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { onClick() }
                .border(
                    width = if (isSelected) 4.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
    }
}