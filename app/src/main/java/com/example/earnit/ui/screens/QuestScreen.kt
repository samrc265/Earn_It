package com.example.earnit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.model.Task
import com.example.earnit.model.TaskType
import com.example.earnit.viewmodel.MainViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun QuestScreen(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    var fireworkTrigger by remember { mutableLongStateOf(0L) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            fun renderSection(title: String, type: TaskType, points: String) {
                val allTasks = tasks.filter { it.type == type }
                val activeTasks = allTasks.filter { !it.isCompleted }
                val completedTasks = allTasks.filter { it.isCompleted }

                item {
                    QuestSection(
                        title = title,
                        points = points,
                        activeTasks = activeTasks,
                        completedTasks = completedTasks,
                        onToggle = { viewModel.toggleTask(it) },
                        onDelete = { viewModel.deleteTask(it) },
                        onCelebrate = { fireworkTrigger = System.currentTimeMillis() }
                    )
                }
            }

            renderSection("Quests", TaskType.DAILY, "2 XP")
            renderSection("Short Term Goals", TaskType.SHORT_TERM, "10 XP")
            renderSection("Long Term Goals", TaskType.LONG_TERM, "25 XP")
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }

        FireworksOverlay(trigger = fireworkTrigger)
    }

    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, type -> viewModel.addTask(name, type) }
        )
    }
}

@Composable
fun QuestSection(
    title: String,
    points: String,
    activeTasks: List<Task>,
    completedTasks: List<Task>,
    onToggle: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onCelebrate: () -> Unit
) {
    var completedExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.animateContentSize(animationSpec = tween(600, easing = FastOutSlowInEasing)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(title, points)

        if (activeTasks.isEmpty()) {
            Text(
                "No active quests.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        } else {
            activeTasks.forEach { task ->
                key(task.id) {
                    TaskItem(
                        task = task,
                        onToggle = { onToggle(task) },
                        onDelete = { onDelete(task) },
                        onCelebrate = onCelebrate
                    )
                }
            }
        }

        if (completedTasks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { completedExpanded = !completedExpanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completed (${completedTasks.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (completedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = completedExpanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    completedTasks.forEach { task ->
                        key(task.id) {
                            TaskItem(
                                task = task,
                                onToggle = { onToggle(task) },
                                onDelete = { onDelete(task) },
                                onCelebrate = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Text(subtitle, modifier = Modifier.padding(4.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit, onCelebrate: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val transition = updateTransition(targetState = task.isCompleted, label = "TaskCompletion")

    // Define colors as simple values to avoid Composable context issues inside transitionSpec
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val goldColor = Color(0xFFFFF7D0)

    val backgroundColor by transition.animateColor(
        label = "BgColor",
        transitionSpec = {
            if (targetState) {
                keyframes {
                    durationMillis = 1000
                    surfaceColor at 0
                    goldColor at 300
                    surfaceVariantColor.copy(alpha=0.6f) at 1000
                }
            } else {
                tween(500)
            }
        }
    ) { completed ->
        if (completed) surfaceVariantColor.copy(alpha=0.6f)
        else surfaceColor
    }

    val contentColor by transition.animateColor(
        label = "ContentColor",
        transitionSpec = { tween(durationMillis = 1000) }
    ) { completed ->
        if (completed) onSurfaceVariantColor.copy(alpha = 0.6f)
        else onSurfaceColor
    }

    val scale by transition.animateFloat(
        label = "Scale",
        transitionSpec = {
            if (targetState) {
                keyframes {
                    durationMillis = 800
                    1f at 0
                    1.12f at 300
                    0.95f at 800
                }
            } else {
                spring(stiffness = Spring.StiffnessLow)
            }
        }
    ) { completed ->
        if (completed) 0.98f else 1f
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = {
                        if (!task.isCompleted) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCelebrate()
                        }
                        onToggle()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Text(
                text = task.name,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = contentColor
            )

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha=0.5f) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun FireworksOverlay(trigger: Long) {
    val particles1 = remember { List(80) { Particle() } }
    val particles2 = remember { List(80) { Particle() } }
    val particles3 = remember { List(80) { Particle() } }

    val animatable1 = remember { Animatable(0f) }
    val animatable2 = remember { Animatable(0f) }
    val animatable3 = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            animatable1.snapTo(0f)
            animatable2.snapTo(0f)
            animatable3.snapTo(0f)
            animatable1.animateTo(1f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
        }
    }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            delay(150)
            animatable2.animateTo(1f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
        }
    }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            delay(300)
            animatable3.animateTo(1f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
        }
    }

    // Theme-based colors
    val themeColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.inversePrimary
    )
    val goldColor = Color(0xFFFFD700)

    // Remember shuffled colors for consistent burst
    val allFireworksColors = remember(trigger) { (themeColors + goldColor).shuffled() }

    if (animatable1.value > 0f || animatable2.value > 0f || animatable3.value > 0f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (animatable1.value > 0f && animatable1.value < 1f) {
                drawBurst(
                    particles = particles1,
                    progress = animatable1.value,
                    center = Offset(x = width / 2, y = height / 3),
                    colors = allFireworksColors
                )
            }

            if (animatable2.value > 0f && animatable2.value < 1f) {
                drawBurst(
                    particles = particles2,
                    progress = animatable2.value,
                    center = Offset(x = width / 4, y = height / 2.5f),
                    colors = allFireworksColors
                )
            }

            if (animatable3.value > 0f && animatable3.value < 1f) {
                drawBurst(
                    particles = particles3,
                    progress = animatable3.value,
                    center = Offset(x = width * 0.75f, y = height / 2.5f),
                    colors = allFireworksColors
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBurst(
    particles: List<Particle>,
    progress: Float,
    center: Offset,
    colors: List<Color>
) {
    particles.forEachIndexed { index, particle ->
        val distance = (300f * progress * particle.speed)
        val gravity = 400f * progress * progress

        val x = center.x + (cos(particle.angle) * distance).toFloat()
        val y = center.y + (sin(particle.angle) * distance).toFloat() + gravity

        val alpha = (1f - progress).coerceIn(0f, 1f)
        val radius = (6.dp.toPx() * (1f - progress) * particle.sizeScale)

        val color = colors[index % colors.size]

        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = Offset(x = x, y = y)
        )
    }
}

private data class Particle(
    val angle: Double = Random.nextDouble(0.0, 2 * Math.PI),
    val speed: Float = Random.nextFloat() * 1.5f + 0.5f,
    val sizeScale: Float = Random.nextFloat() * 0.8f + 0.2f,
    val colorVariant: Int = Random.nextInt(0, 3)
)

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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Difficulty / Type:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.replace("_", " ").take(5)) }
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
            }) { Text("Add Quest") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}