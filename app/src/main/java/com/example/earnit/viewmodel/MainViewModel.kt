package com.example.earnit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.earnit.EarnItApplication
import com.example.earnit.data.EarnItRepository
import com.example.earnit.model.RewardNote
import com.example.earnit.model.Task
import com.example.earnit.model.TaskType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: EarnItRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val score: StateFlow<Int> = repository.score
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notes: StateFlow<List<RewardNote>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeIndex: StateFlow<Int> = repository.themeIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val darkMode: StateFlow<Int> = repository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Actions ---

    fun setTheme(index: Int) {
        viewModelScope.launch { repository.updateTheme(index) }
    }

    fun setDarkMode(mode: Int) {
        viewModelScope.launch { repository.updateDarkMode(mode) }
    }

    fun addTask(name: String, type: TaskType) {
        val points = when (type) {
            TaskType.DAILY -> 2
            TaskType.SHORT_TERM -> 10
            TaskType.LONG_TERM -> 50
        }
        viewModelScope.launch {
            repository.addTask(Task(name = name, type = type, points = points))
        }
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
            if (task.isCompleted) {
                repository.updateScore(-task.points)
            }
            repository.deleteTask(task)
        }
    }

    fun addNote(content: String): Boolean {
        val currentScore = score.value
        if (currentScore >= 100) {
            viewModelScope.launch {
                repository.updateScore(-100)
                repository.addNote(RewardNote(content = content))
            }
            return true
        }
        return false
    }

    fun updateNote(note: RewardNote, newContent: String) {
        viewModelScope.launch {
            repository.updateNote(note.copy(content = newContent))
        }
    }

    fun deleteNote(note: RewardNote) {
        viewModelScope.launch {
            repository.updateScore(100)
            repository.deleteNote(note)
        }
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