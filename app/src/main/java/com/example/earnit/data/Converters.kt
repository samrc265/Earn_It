package com.example.earnit.data

import androidx.room.TypeConverter
import com.example.earnit.model.TaskType

class Converters {
    @TypeConverter
    fun fromTaskType(value: TaskType): String {
        return value.name
    }

    @TypeConverter
    fun toTaskType(value: String): TaskType {
        return try {
            TaskType.valueOf(value)
        } catch (e: Exception) {
            TaskType.DAILY // Fallback
        }
    }
}