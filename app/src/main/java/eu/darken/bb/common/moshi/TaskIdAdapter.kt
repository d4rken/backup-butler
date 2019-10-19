package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.task.core.Task
import java.util.*

class TaskIdAdapter {
    @ToJson
    fun toJson(item: Task.Id): String = item.value.toString()

    @FromJson
    fun fromJson(item: String): Task.Id = Task.Id(UUID.fromString(item))
}
