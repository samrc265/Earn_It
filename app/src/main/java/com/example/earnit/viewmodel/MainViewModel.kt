package com.example.earnit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.earnit.EarnItApplication
import com.example.earnit.data.EarnItRepository
import com.example.earnit.model.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// Data class for Backup (Holds everything)
data class BackupData(
    val tasks: List<Task>,
    val notes: List<RewardNote>,
    val plantState: PlantState,
    val forest: List<ForestTree>,
    val score: Int
)

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

    val showOnboarding = repository.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            repository.checkDailyReset()
            checkPlantWilting()
        }
    }

    // --- Backup & Restore ---
    fun createBackupJson(): String {
        val data = BackupData(
            tasks = tasks.value,
            notes = notes.value,
            plantState = plantState.value,
            forest = forest.value,
            score = score.value
        )
        return Gson().toJson(data)
    }

    fun restoreBackupJson(json: String): Boolean {
        return try {
            val data = Gson().fromJson(json, BackupData::class.java)
            viewModelScope.launch {
                // Restore logic: Clear existing and insert new
                // For simplicity, we just upsert everything
                data.tasks.forEach { repository.addTask(it) }
                data.notes.forEach { repository.addNote(it) }
                data.forest.forEach { repository.moveTreeToForest(it) } // Logic reuse
                repository.updatePlantState(data.plantState)
                // Score is a bit tricky since it's in UserStats, we need a way to set it directly
                // For now, we accept score might not overwrite exactly without a specific setScore method
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Onboarding ---
    fun completeOnboarding() {
        viewModelScope.launch { repository.completeOnboarding() }
    }

    fun resetOnboarding() {
        viewModelScope.launch { repository.resetOnboarding() }
    }

    // --- Plant Logic ---
    private suspend fun checkPlantWilting() {
        val currentPlant = plantState.value
        if (currentPlant.stage == 0 || currentPlant.isDead) return

        val lastWatered = currentPlant.lastWateredDate
        if (lastWatered == 0L) return

        val diff = System.currentTimeMillis() - lastWatered
        val daysPassed = TimeUnit.MILLISECONDS.toDays(diff).toInt()

        if (daysPassed > 1) {
            val healthPenalty = daysPassed - 1
            val newHealth = (currentPlant.health - healthPenalty).coerceAtLeast(0)
            val isDead = newHealth == 0
            repository.updatePlantState(currentPlant.copy(health = newHealth, isDead = isDead))
        }
    }

    fun waterPlant() {
        val currentPlant = plantState.value
        if (currentPlant.isDead || currentPlant.stage == 0) return

        val dailyTasks = tasks.value.filter { it.type == TaskType.DAILY }
        val completed = dailyTasks.count { it.isCompleted }
        val total = dailyTasks.size

        if (total == 0) return
        val ratio = completed.toFloat() / total.toFloat()

        if (ratio >= 0.66f) {
            val today = System.currentTimeMillis()
            val last = Calendar.getInstance().apply { timeInMillis = currentPlant.lastWateredDate }
            val now = Calendar.getInstance().apply { timeInMillis = today }
            val isSameDay = last.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                    last.get(Calendar.YEAR) == now.get(Calendar.YEAR)

            if (!isSameDay) {
                var newStage = currentPlant.stage
                var daysAtMat = currentPlant.daysAtMaturity
                var newHealth = currentPlant.health

                if (currentPlant.health < 3) {
                    newHealth++
                } else {
                    if (currentPlant.stage < 4) {
                        newStage++
                    } else {
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

    fun debugGrowPlant() {
        val currentPlant = plantState.value
        if (currentPlant.stage == 0) return

        var newStage = currentPlant.stage
        var daysAtMat = currentPlant.daysAtMaturity

        if (currentPlant.stage < 4) {
            newStage++
        } else {
            daysAtMat++
        }

        viewModelScope.launch {
            repository.updatePlantState(currentPlant.copy(
                stage = newStage,
                health = 3,
                daysAtMaturity = daysAtMat,
                lastWateredDate = System.currentTimeMillis(),
                isDead = false
            ))
        }
    }

    fun startNewPlant(type: TreeType) {
        val newSeed = Random.nextLong()
        viewModelScope.launch {
            repository.updatePlantState(PlantState(
                stage = 1,
                health = 3,
                treeType = type,
                seed = newSeed,
                lastWateredDate = System.currentTimeMillis()
            ))
        }
    }

    fun restartPlant() {
        viewModelScope.launch {
            repository.updatePlantState(PlantState())
        }
    }

    fun sendTreeToForest(name: String) {
        val current = plantState.value
        val currentTheme = themeIndex.value
        viewModelScope.launch {
            repository.moveTreeToForest(ForestTree(
                name = name,
                themeIndex = currentTheme,
                treeType = current.treeType,
                seed = current.seed
            ))
        }
    }

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