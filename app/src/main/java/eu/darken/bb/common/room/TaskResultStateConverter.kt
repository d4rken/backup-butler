package eu.darken.bb.common.room

import androidx.room.TypeConverter
import eu.darken.bb.task.core.Task

class TaskResultStateConverter {
    @TypeConverter
    fun fromString(id: String?): Task.Result.State? = id?.let { Task.Result.State.fromValue(id) }

    @TypeConverter
    fun toStringValue(id: Task.Result.State?): String? = id?.value
}