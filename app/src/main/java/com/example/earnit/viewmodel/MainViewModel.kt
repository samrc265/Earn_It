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

    // --- State (Connected to Database) ---
    // These automatically update when the database changes
    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val score: StateFlow<Int> = repository.score
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notes: StateFlow<List<RewardNote>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---
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
            // 1. Update the task status
            repository.updateTask(task.copy(isCompleted = newStatus))

            // 2. Calculate point change (Add points if checking, subtract if unchecking)
            val pointsDelta = if (newStatus) task.points else -task.points
            repository.updateScore(pointsDelta)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            // Optional: If you delete a completed task, should you lose the points? 
            // Currently: Yes.
            if (task.isCompleted) {
                repository.updateScore(-task.points)
            }
            repository.deleteTask(task)
        }
    }

    fun addNote(content: String) {
        viewModelScope.launch {
            repository.addNote(RewardNote(content = content))
        }
    }

    fun deleteNote(note: RewardNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- Factory ---
    // This tells Android how to create this ViewModel with the Repository dependency
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EarnItApplication)
                MainViewModel(application.repository)
            }
        }
    }
}