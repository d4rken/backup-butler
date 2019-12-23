package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.results.TaskResult

class TaskResultIdConverter {
    @TypeConverter
    fun fromString(id: String?): TaskResult.Id? = id?.let { TaskResult.Id(id) }

    @TypeConverter
    fun toStringValue(id: TaskResult.Id?): String? = id?.value?.toString()
}