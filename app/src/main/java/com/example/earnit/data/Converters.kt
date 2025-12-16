package com.example.earnit.data

import androidx.room.TypeConverter
import com.example.earnit.model.TaskType
import com.example.earnit.model.TreeType

class Converters {
    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = try {
        TaskType.valueOf(value)
    } catch (e: Exception) {
        TaskType.DAILY
    }

    @TypeConverter
    fun fromTreeType(value: TreeType): String = value.name

    @TypeConverter
    fun toTreeType(value: String): TreeType = try {
        TreeType.valueOf(value)
    } catch (e: Exception) {
        TreeType.PINE
    }
}