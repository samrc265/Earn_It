package com.example.earnit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.earnit.EarnItApplication
import com.example.earnit.data.EarnItRepository
import com.example.earnit.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainViewModel(private val repository: EarnItRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plantState: StateFlow<PlantState> = repository.plantState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlantState())

    val forest: StateFlow<List<ForestTree>> = repository.forest
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val score = repository.score.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val notes = repository.notes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val themeIndex = repository.themeIndex.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val darkMode = repository.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repository.checkDailyReset()
            checkPlantWilting()
        }
    }

    // --- Plant Logic ---

    private suspend fun checkPlantWilting() {
        val currentPlant = plantState.value
        if (currentPlant.stage == 0 || currentPlant.isDead) return // Seeds don't wilt

        val lastWatered = currentPlant.lastWateredDate
        if (lastWatered == 0L) return

        val diff = System.currentTimeMillis() - lastWatered
        val daysPassed = TimeUnit.MILLISECONDS.toDays(diff).toInt()

        if (daysPassed > 1) {
            // Missed at least 1 day.
            // Logic: 4 days total to kill.
            // Healthy(3) -> Wilt1(2) -> Wilt2(1) -> Dead(0)

            // If daysPassed is 2 (missed 1 day), subtract 1 health
            // If daysPassed is 4 (missed 3 days), subtract 3 health (Death)
            val healthPenalty = daysPassed - 1
            val newHealth = (currentPlant.health - healthPenalty).coerceAtLeast(0)

            val isDead = newHealth == 0

            repository.updatePlantState(currentPlant.copy(
                health = newHealth,
                isDead = isDead,
                // If dead, reset stage? User prompt usually handles this,
                // but let's keep stage to show the dead tree until they reset.
            ))
        }
    }

    fun waterPlant() {
        val currentPlant = plantState.value
        if (currentPlant.isDead) return

        // Check 2/3rds Daily Task Completion
        val dailyTasks = tasks.value.filter { it.type == TaskType.DAILY }
        val completed = dailyTasks.count { it.isCompleted }
        val total = dailyTasks.size

        // Prevent watering if no tasks exist or logic not met
        if (total == 0) return
        val ratio = completed.toFloat() / total.toFloat()

        if (ratio >= 0.66f) {
            val today = System.currentTimeMillis()

            // Check if already watered today
            val last = Calendar.getInstance().apply { timeInMillis = currentPlant.lastWateredDate }
            val now = Calendar.getInstance().apply { timeInMillis = today }
            val isSameDay = last.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                    last.get(Calendar.YEAR) == now.get(Calendar.YEAR)

            if (!isSameDay) {
                // WATERING SUCCESS
                var newStage = currentPlant.stage
                var daysAtMat = currentPlant.daysAtMaturity

                // Grow logic: If healthy, grow. If wilted, recover health first.
                var newHealth = currentPlant.health

                if (currentPlant.health < 3) {
                    newHealth++ // Recover health
                } else {
                    // Fully healthy, so we grow
                    if (currentPlant.stage < 4) {
                        newStage++
                    } else {
                        // Already a tree, increase maturity counter
                        daysAtMat++
                    }
                }

                viewModelScope.launch {
                    repository.updatePlantState(currentPlant.copy(
                        stage = newStage,
                        health = newHealth,
                        daysAtMaturity = daysAtMat,
                        lastWateredDate = today
                    ))
                }
            }
        }
    }

    fun restartPlant() {
        viewModelScope.launch {
            repository.updatePlantState(PlantState()) // Reset to defaults
        }
    }

    fun sendTreeToForest(name: String) {
        val currentTheme = themeIndex.value
        viewModelScope.launch {
            repository.moveTreeToForest(ForestTree(name = name, themeIndex = currentTheme))
        }
    }

    // --- Standard Actions ---
    fun setTheme(index: Int) = viewModelScope.launch { repository.updateTheme(index) }
    fun setDarkMode(mode: Int) = viewModelScope.launch { repository.updateDarkMode(mode) }

    fun addTask(name: String, type: TaskType) {
        val points = when (type) {
            TaskType.DAILY -> 2
            TaskType.SHORT_TERM -> 10
            TaskType.LONG_TERM -> 25
        }
        viewModelScope.launch { repository.addTask(Task(name = name, type = type, points = points)) }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = newStatus))
            val pointsDelta = if (newStatus) task.points else -task.points
            repository.updateScore(pointsDelta)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) repository.updateScore(-task.points)
            repository.deleteTask(task)
        }
    }

    fun addNote(content: String): Boolean {
        if (score.value >= 100) {
            viewModelScope.launch {
                repository.updateScore(-100)
                repository.addNote(RewardNote(content = content))
            }
            return true
        }
        return false
    }

    fun updateNote(note: RewardNote, newContent: String) = viewModelScope.launch { repository.updateNote(note.copy(content = newContent)) }
    fun deleteNote(note: RewardNote) = viewModelScope.launch {
        repository.updateScore(100)
        repository.deleteNote(note)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EarnItApplication)
                MainViewModel(application.repository)
            }
        }
    }
}