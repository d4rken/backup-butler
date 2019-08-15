package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.Task

class TaskResultIdConverter {
    @TypeConverter
    fun fromString(id: String?): Task.Result.Id? = id?.let { Task.Result.Id(id) }

    @TypeConverter
    fun toStringValue(id: Task.Result.Id?): String? = id?.id?.toString()
}