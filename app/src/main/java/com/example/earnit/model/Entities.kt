package com.example.earnit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TaskType {
    DAILY,
    SHORT_TERM,
    LONG_TERM
}

@Entity(tableName = "tasks")
data class Task(
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

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 0,
    val score: Int = 0,
    val themeIndex: Int = 0 // 0=Purple, 1=Ocean, 2=Nature, 3=Sunset
)