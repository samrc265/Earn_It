package com.example.earnit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TaskType {
    DAILY,
    SHORT_TERM,
    LONG_TERM
}

enum class TreeType {
    PINE,
    CHERRY_BLOSSOM,
    CACTUS,
    ORANGE
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

@Entity(tableName = "plant_state")
data class PlantState(
    @PrimaryKey val id: Int = 0,
    val stage: Int = 0,
    val health: Int = 3,
    val lastWateredDate: Long = 0L,
    val daysAtMaturity: Int = 0,
    val isDead: Boolean = false,
    val treeType: TreeType = TreeType.PINE,
    val seed: Long = 0L,
    val wateringStreak: Int = 0 // New Field
)

@Entity(tableName = "forest")
data class ForestTree(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateCompleted: Long = System.currentTimeMillis(),
    val themeIndex: Int,
    val treeType: TreeType,
    val seed: Long
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 0,
    val score: Int = 0,
    val themeIndex: Int = 0,
    val darkMode: Int = 0,
    val lastLoginDate: Long = 0L,
    val isFirstLaunch: Boolean = true
)