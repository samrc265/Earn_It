package com.example.earnit.data

import com.example.earnit.model.RewardNote
import com.example.earnit.model.Task
import com.example.earnit.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class EarnItRepository(private val dao: EarnItDao) {

    val tasks: Flow<List<Task>> = dao.getAllTasks()
    val notes: Flow<List<RewardNote>> = dao.getAllNotes()

    // Get simple streams of data for the ViewModel
    private val _userStats = dao.getUserStats()

    val score: Flow<Int> = _userStats.map { it?.score ?: 0 }
    val themeIndex: Flow<Int> = _userStats.map { it?.themeIndex ?: 0 }

    suspend fun addTask(task: Task) = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)

    suspend fun addNote(note: RewardNote) = dao.insertNote(note)
    suspend fun updateNote(note: RewardNote) = dao.updateNote(note)
    suspend fun deleteNote(note: RewardNote) = dao.deleteNote(note)

    // Helper to update specific fields in UserStats without overwriting others
    private suspend fun updateUserStats(transform: (UserStats) -> UserStats) {
        val current = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertOrUpdateStats(transform(current))
    }

    suspend fun updateScore(delta: Int) {
        updateUserStats { it.copy(score = (it.score + delta).coerceAtLeast(0)) }
    }

    suspend fun updateTheme(index: Int) {
        updateUserStats { it.copy(themeIndex = index) }
    }
}