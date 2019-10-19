package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.Task

class TaskIdConverter {
    @TypeConverter
    fun fromString(id: String?): Task.Id? = id?.let { Task.Id(id) }

    @TypeConverter
    fun toStringValue(id: Task.Id?): String? = id?.value?.toString()
}