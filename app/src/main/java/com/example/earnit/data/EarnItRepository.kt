package com.example.earnit.data

import com.example.earnit.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class EarnItRepository(private val dao: EarnItDao) {

    val tasks: Flow<List<Task>> = dao.getAllTasks()
    val notes: Flow<List<RewardNote>> = dao.getAllNotes()

    val plantState: Flow<PlantState> = dao.getPlantState().map { it ?: PlantState() }
    val forest: Flow<List<ForestTree>> = dao.getForest()

    private val _userStats = dao.getUserStats()

    val score: Flow<Int> = _userStats.map { it?.score ?: 0 }
    val themeIndex: Flow<Int> = _userStats.map { it?.themeIndex ?: 0 }
    val darkMode: Flow<Int> = _userStats.map { it?.darkMode ?: 0 }
    val isFirstLaunch: Flow<Boolean> = _userStats.map { it?.isFirstLaunch ?: true }

    // --- Tasks ---
    suspend fun addTask(task: Task) = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    suspend fun getTaskById(id: String): Task? = dao.getTaskById(id) // New

    suspend fun updatePlantState(plant: PlantState) = dao.insertOrUpdatePlant(plant)
    suspend fun moveTreeToForest(tree: ForestTree) {
        dao.addToForest(tree)
        dao.insertOrUpdatePlant(PlantState())
    }

    suspend fun checkDailyReset() {
        val stats = dao.getUserStats().firstOrNull() ?: UserStats()
        val lastLogin = Calendar.getInstance().apply { timeInMillis = stats.lastLoginDate }
        val today = Calendar.getInstance()

        val isSameDay = lastLogin.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                lastLogin.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

        if (!isSameDay) {
            dao.resetDailyTasks()
            updateUserStats { it.copy(lastLoginDate = System.currentTimeMillis()) }
        }
    }

    suspend fun addNote(note: RewardNote) = dao.insertNote(note)
    suspend fun updateNote(note: RewardNote) = dao.updateNote(note)
    suspend fun deleteNote(note: RewardNote) = dao.deleteNote(note)

    private suspend fun updateUserStats(transform: (UserStats) -> UserStats) {
        val current = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertOrUpdateStats(transform(current))
    }

    suspend fun updateScore(delta: Int) = updateUserStats { it.copy(score = (it.score + delta).coerceAtLeast(0)) }
    suspend fun updateTheme(index: Int) = updateUserStats { it.copy(themeIndex = index) }
    suspend fun updateDarkMode(mode: Int) = updateUserStats { it.copy(darkMode = mode) }

    suspend fun completeOnboarding() = updateUserStats { it.copy(isFirstLaunch = false) }
    suspend fun resetOnboarding() = updateUserStats { it.copy(isFirstLaunch = true) }
}