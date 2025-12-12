package com.example.earnit.data

import androidx.room.*
import com.example.earnit.model.RewardNote
import com.example.earnit.model.Task
import com.example.earnit.model.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface EarnItDao {

    // --- Tasks ---
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // --- Notes ---
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<RewardNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: RewardNote)

    @Update
    suspend fun updateNote(note: RewardNote) // Added this for editing

    @Delete
    suspend fun deleteNote(note: RewardNote)

    // --- Stats (Score) ---
    @Query("SELECT * FROM user_stats WHERE id = 0")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)
}