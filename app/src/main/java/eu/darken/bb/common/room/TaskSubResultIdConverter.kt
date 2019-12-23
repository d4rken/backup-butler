package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.results.TaskResult

class TaskSubResultIdConverter {
    @TypeConverter
    fun fromString(id: String?): TaskResult.SubResult.Id? = id?.let { TaskResult.SubResult.Id(id) }

    @TypeConverter
    fun toStringValue(id: TaskResult.SubResult.Id?): String? = id?.value?.toString()
}