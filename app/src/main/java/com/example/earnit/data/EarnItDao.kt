package com.example.earnit.data

import androidx.room.*
import com.example.earnit.model.*
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

    // Reset Logic: Uncheck all Daily tasks
    @Query("UPDATE tasks SET isCompleted = 0 WHERE type = 'DAILY'")
    suspend fun resetDailyTasks()

    // --- Plant ---
    @Query("SELECT * FROM plant_state WHERE id = 0")
    fun getPlantState(): Flow<PlantState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlant(plant: PlantState)

    // --- Forest ---
    @Query("SELECT * FROM forest ORDER BY dateCompleted DESC")
    fun getForest(): Flow<List<ForestTree>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToForest(tree: ForestTree)

    // --- Notes ---
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<RewardNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: RewardNote)

    @Update
    suspend fun updateNote(note: RewardNote)

    @Delete
    suspend fun deleteNote(note: RewardNote)

    // --- Stats ---
    @Query("SELECT * FROM user_stats WHERE id = 0")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStats)
}