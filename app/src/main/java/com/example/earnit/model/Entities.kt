package com.example.earnit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TaskType {
    DAILY,
    SHORT_TERM,
    LONG_TERM
}

// @Entity tells Room: "Create a database table named 'tasks' for this class"
@Entity(tableName = "tasks")
data class Task(
    // @PrimaryKey tells Room: "This is the unique ID for this row"
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TaskType,
    val isCompleted: Boolean = false,
    val points: Int
)

@Entity(tableName = "notes")
data class RewardNote(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// We use this to store the global score persistently
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 0, // Always 0, single row table
    val score: Int = 0
)