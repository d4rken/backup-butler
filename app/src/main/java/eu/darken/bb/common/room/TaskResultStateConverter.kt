package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.results.TaskResult

class TaskResultStateConverter {
    @TypeConverter
    fun fromString(id: String?): TaskResult.State? = id?.let { TaskResult.State.fromValue(id) }

    @TypeConverter
    fun toStringValue(id: TaskResult.State?): String? = id?.value
}