package com.example.earnit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.earnit.model.ForestTree
import com.example.earnit.model.PlantState
import com.example.earnit.model.RewardNote
import com.example.earnit.model.Task
import com.example.earnit.model.UserStats

@Database(
    entities = [Task::class, RewardNote::class, UserStats::class, PlantState::class, ForestTree::class],
    version = 4, // Version Bumped for isFirstLaunch
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EarnItDatabase : RoomDatabase() {
    abstract fun dao(): EarnItDao

    companion object {
        @Volatile
        private var Instance: EarnItDatabase? = null

        fun getDatabase(context: Context): EarnItDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, EarnItDatabase::class.java, "earnit_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}