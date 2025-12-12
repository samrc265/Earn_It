package com.example.earnit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.ui.theme.*
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Appearance", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // --- Color Palette ---
        Text("Theme Color", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
        Text("Night Mode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = darkMode == 0,
                onClick = { viewModel.setDarkMode(0) },
                label = { Text("System") }
            )
            FilterChip(
                selected = darkMode == 1,
                onClick = { viewModel.setDarkMode(1) },
                label = { Text("Light") }
            )
            FilterChip(
                selected = darkMode == 2,
                onClick = { viewModel.setDarkMode(2) },
                label = { Text("Dark") }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("About Earn It", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Version 1.0.1", style = MaterialTheme.typography.bodySmall)
                Text("Gamify your life, one task at a time.", style = MaterialTheme.typography.bodySmall)
            }
        }
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