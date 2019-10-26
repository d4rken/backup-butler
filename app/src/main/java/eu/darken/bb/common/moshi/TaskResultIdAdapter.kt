package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.task.core.Task
import java.util.*

class TaskResultIdAdapter {
    @ToJson
    fun toJson(item: Task.Result.Id): String = item.value.toString()

    @FromJson
    fun fromJson(item: String): Task.Result.Id = Task.Result.Id(UUID.fromString(item))
}
