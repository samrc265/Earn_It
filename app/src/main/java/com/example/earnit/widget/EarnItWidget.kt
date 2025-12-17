package com.example.earnit.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.TextDecoration
import androidx.glance.unit.ColorProvider
import com.example.earnit.EarnItApplication
import com.example.earnit.MainActivity
import com.example.earnit.model.Task
import com.example.earnit.model.TaskType
import kotlinx.coroutines.flow.map

class TodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as EarnItApplication).repository

        provideContent {
            val tasksFlow = repository.tasks
            val dailyTasks by tasksFlow.map { it.filter { t -> t.type == TaskType.DAILY } }.collectAsState(initial = emptyList())
            val shortTasks by tasksFlow.map { it.filter { t -> t.type == TaskType.SHORT_TERM } }.collectAsState(initial = emptyList())
            val longTasks by tasksFlow.map { it.filter { t -> t.type == TaskType.LONG_TERM } }.collectAsState(initial = emptyList())

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Earn It Quests",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (dailyTasks.isEmpty() && shortTasks.isEmpty() && longTasks.isEmpty()) {
                        Box(contentAlignment = Alignment.Center, modifier = GlanceModifier.fillMaxSize()) {
                            Text("No quests yet.", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
                        }
                    } else {
                        LazyColumn {
                            fun renderSection(title: String, tasks: List<Task>) {
                                if (tasks.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = title,
                                            style = TextStyle(
                                                color = GlanceTheme.colors.secondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = GlanceModifier.padding(top = 8.dp, bottom = 4.dp)
                                        )
                                    }
                                    items(tasks.sortedBy { it.isCompleted }) { task ->
                                        TaskRow(task)
                                    }
                                }
                            }

                            renderSection("Daily", dailyTasks)
                            renderSection("Short Term", shortTasks)
                            renderSection("Long Term", longTasks)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: Task) {
    val style = if (task.isCompleted) {
        TextStyle(
            color = GlanceTheme.colors.onSurfaceVariant,
            textDecoration = TextDecoration.LineThrough
        )
    } else {
        TextStyle(color = GlanceTheme.colors.onSurface)
    }

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(actionRunCallback<ToggleTaskAction>(actionParametersOf(ToggleTaskAction.taskIdKey to task.id))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (task.isCompleted) "☑" else "⬜",
            style = TextStyle(fontSize = 16.sp, color = GlanceTheme.colors.primary)
        )
        Text(
            text = task.name,
            style = style,
            modifier = GlanceModifier.padding(start = 8.dp)
        )
    }
}

class ToggleTaskAction : ActionCallback {
    companion object {
        val taskIdKey = ActionParameters.Key<String>("taskId")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[taskIdKey] ?: return
        val repository = (context.applicationContext as EarnItApplication).repository

        val task = repository.getTaskById(taskId) ?: return
        val newStatus = !task.isCompleted

        repository.updateTask(task.copy(isCompleted = newStatus))

        val pointsDelta = if (newStatus) task.points else -task.points
        repository.updateScore(pointsDelta)

        TodoWidget().updateAll(context)
        PlantWidget().updateAll(context)
    }
}

class PlantWidget : GlanceAppWidget() {
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as EarnItApplication).repository

        provideContent {
            val plantState by repository.plantState.collectAsState(initial = null)

            GlanceTheme {
                // CHANGED TO ROW FOR HORIZONTAL ORIENTATION
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(8.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (plantState == null) {
                        Text("...", style = TextStyle(color = GlanceTheme.colors.onSurface))
                    } else {
                        val p = plantState!!
                        val icon = when {
                            p.isDead -> "🥀"
                            p.health < 3 -> "🍂"
                            p.stage == 0 -> "🌱"
                            p.stage == 1 -> "🌱"
                            p.stage == 2 -> "🌿"
                            p.stage == 3 -> "🪴"
                            p.stage == 4 -> "🌳"
                            else -> "🌱"
                        }

                        // Icon on Left
                        Text(
                            text = icon,
                            style = TextStyle(fontSize = 32.sp),
                            modifier = GlanceModifier.padding(end = 12.dp)
                        )

                        // Text Info on Right
                        Column(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Streak",
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = if (p.isDead) "0" else "${p.wateringStreak}",
                                style = TextStyle(
                                    color = if (p.isDead) ColorProvider(Color.Red) else GlanceTheme.colors.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}

class PlantWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PlantWidget()
}