package com.example.earnit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.model.TaskType
import com.example.earnit.ui.components.PixelTree
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun PlantScreen(viewModel: MainViewModel, onNavigateToForest: () -> Unit) {
    val plantState by viewModel.plantState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val themeIndex by viewModel.themeIndex.collectAsStateWithLifecycle()

    // Calculate Task Progress
    val dailyTasks = tasks.filter { it.type == TaskType.DAILY }
    val completedCount = dailyTasks.count { it.isCompleted }
    val totalCount = dailyTasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val canWater = progress >= 0.66f

    var showHarvestDialog by remember { mutableStateOf(false) }

    // Check for harvest
    LaunchedEffect(plantState) {
        if (plantState.stage == 4 && plantState.daysAtMaturity >= 3) {
            showHarvestDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- Forest Button (Top Left) ---
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart) // Changed to TopStart
                .zIndex(1f)
        ) {
            SmallFloatingActionButton(
                onClick = onNavigateToForest,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                // Using Emoji for Tree icon
                Text("🌲", fontSize = 24.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- The Plant Environment ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Sky Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )

                    // Ground
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                    )

                    // The Tree
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        PixelTree(
                            stage = plantState.stage,
                            health = plantState.health,
                            themeIndex = themeIndex,
                            modifier = Modifier.size(220.dp),
                            pixelSize = 8.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Status Section ---
            val statusText = when {
                plantState.isDead -> "Withered"
                plantState.health < 3 -> "Needs Water"
                plantState.stage == 4 -> "Mature (${plantState.daysAtMaturity}/3 Days)"
                else -> "Healthy"
            }

            val statusColor = when {
                plantState.isDead -> MaterialTheme.colorScheme.error
                plantState.health < 3 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Action Controls ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (plantState.isDead) {
                    // Restart Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilledIconButton(
                            onClick = { viewModel.restartPlant() },
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Restart",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Replant", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    // Water/Grow Button
                    Box(contentAlignment = Alignment.Center) {
                        // Circular Progress Indicator
                        val animatedProgress by animateFloatAsState(targetValue = progress, label = "DailyProgress")

                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(88.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 6.dp
                        )

                        FilledIconButton(
                            onClick = { viewModel.waterPlant() },
                            enabled = canWater,
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (canWater) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (canWater) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            // Using Favorite (Heart) to symbolize caring/watering
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Water",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Instruction Text below actions
            Spacer(modifier = Modifier.height(16.dp))
            if (!plantState.isDead) {
                Text(
                    text = if (canWater) "Ready to water!" else "${completedCount}/${(totalCount * 0.66).toInt() + 1} daily quests needed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    if (showHarvestDialog) {
        HarvestDialog(
            onDismiss = { showHarvestDialog = false },
            onConfirm = { name ->
                viewModel.sendTreeToForest(name)
                showHarvestDialog = false
            }
        )
    }
}

@Composable
fun HarvestDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tree Matured!") },
        text = {
            Column {
                Text("Your tree is ready for the forest. Give it a name to remember it by.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tree Name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Move to Forest") }
        }
    )
}