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

// Tracks the current active plant
@Entity(tableName = "plant_state")
data class PlantState(
    @PrimaryKey val id: Int = 0, // Single row
    val stage: Int = 0, // 0=Seed, 1=Sapling, 2=Small, 3=Medium, 4=Tree
    val health: Int = 3, // 3=Healthy, 2=Wilted1, 1=Wilted2, 0=Dead
    val lastWateredDate: Long = 0L, // Timestamp of last watering
    val daysAtMaturity: Int = 0, // Counts days kept alive at Stage 4
    val isDead: Boolean = false
)

// Archived trees
@Entity(tableName = "forest")
data class ForestTree(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateCompleted: Long = System.currentTimeMillis(),
    val themeIndex: Int // Keeps the color it was grown with
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 0,
    val score: Int = 0,
    val themeIndex: Int = 0,
    val darkMode: Int = 0,
    val lastLoginDate: Long = 0L // To calculate missed days/wilting
)