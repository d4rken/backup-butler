package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.Task

class TaskTypeConverter {
    @TypeConverter
    fun fromString(id: String?) = id?.let { Task.Type.fromValue(id) }

    @TypeConverter
    fun toStringValue(id: Task.Type?) = id?.value
}