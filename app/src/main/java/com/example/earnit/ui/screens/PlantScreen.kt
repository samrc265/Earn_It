package com.example.earnit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.model.TaskType
import com.example.earnit.model.TreeType
import com.example.earnit.ui.components.PixelTree
import com.example.earnit.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun PlantScreen(viewModel: MainViewModel, onNavigateToForest: () -> Unit) {
    val plantState by viewModel.plantState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    if (plantState.stage == 0) {
        SeedSelectionScreen(onPlant = { type -> viewModel.startNewPlant(type) }, onNavigateToForest = onNavigateToForest)
        return
    }

    // Logic to check if already watered TODAY
    val isWateredToday = remember(plantState.lastWateredDate) {
        val last = Calendar.getInstance().apply { timeInMillis = plantState.lastWateredDate }
        val now = Calendar.getInstance()
        last.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                last.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    val dailyTasks = tasks.filter { it.type == TaskType.DAILY }
    val completedCount = dailyTasks.count { it.isCompleted }
    val totalCount = dailyTasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    // Can water if: Progress > 66% AND hasn't watered today AND not dead
    val canWater = progress >= 0.66f && !isWateredToday && !plantState.isDead

    var showHarvestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(plantState) {
        if (plantState.stage == 4 && plantState.daysAtMaturity >= 3) {
            showHarvestDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).zIndex(1f)
        ) {
            SmallFloatingActionButton(
                onClick = onNavigateToForest,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
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

            Card(
                modifier = Modifier.fillMaxWidth().height(380.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.surface)
                        )
                    ))
                    Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)))
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
                        PixelTree(stage = plantState.stage, health = plantState.health, type = plantState.treeType, seed = plantState.seed, modifier = Modifier.size(250.dp), pixelSize = 4.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val statusText = when {
                plantState.isDead -> "Withered"
                isWateredToday -> "Watered Today"
                plantState.health < 3 -> "Needs Water"
                plantState.stage == 4 -> "Mature (${plantState.daysAtMaturity}/3 Days)"
                else -> "Growing (${plantState.treeType.name.replace("_", " ").take(5)})"
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (plantState.isDead || plantState.health < 3 && !isWateredToday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (plantState.isDead) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilledIconButton(
                            onClick = { viewModel.restartPlant() },
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Replant", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
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
                            // Change Icon if watered
                            Icon(
                                if (isWateredToday) Icons.Default.Favorite else Icons.Default.Favorite, // Keep heart, but color changes indicate state
                                contentDescription = "Water",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (!plantState.isDead) {
                val subText = when {
                    isWateredToday -> "Come back tomorrow!"
                    canWater -> "Ready to water!"
                    else -> "${completedCount}/${(totalCount * 0.66).toInt() + 1} daily quests needed"
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

    if (showHarvestDialog) {
        HarvestDialog(onDismiss = { showHarvestDialog = false }, onConfirm = { name -> viewModel.sendTreeToForest(name); showHarvestDialog = false })
    }
}

@Composable
fun SeedSelectionScreen(onPlant: (TreeType) -> Unit, onNavigateToForest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).zIndex(1f)
        ) {
            SmallFloatingActionButton(
                onClick = onNavigateToForest,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) { Text("🌲", fontSize = 24.sp) }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Choose a Seed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("What will you grow next?", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(TreeType.values()) { type ->
                    SeedCard(type = type, onClick = { onPlant(type) })
                }
            }
        }
    }
}

@Composable
fun SeedCard(type: TreeType, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                PixelTree(
                    stage = 4,
                    type = type,
                    seed = 12345,
                    modifier = Modifier.fillMaxSize(),
                    pixelSize = null
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
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