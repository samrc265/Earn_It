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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Appearance", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Choose your vibe", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(32.dp))

        // Color Picker Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThemeCircle(color = Purple40, isSelected = themeIndex == 0, onClick = { viewModel.setTheme(0) }, label = "Classic")
            ThemeCircle(color = OceanPrimary, isSelected = themeIndex == 1, onClick = { viewModel.setTheme(1) }, label = "Ocean")
            ThemeCircle(color = NaturePrimary, isSelected = themeIndex == 2, onClick = { viewModel.setTheme(2) }, label = "Nature")
            ThemeCircle(color = SunsetPrimary, isSelected = themeIndex == 3, onClick = { viewModel.setTheme(3) }, label = "Sunset")
        }

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("About Earn It", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall)
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