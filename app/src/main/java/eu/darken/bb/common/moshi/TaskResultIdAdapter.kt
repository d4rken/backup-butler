package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.task.core.results.TaskResult
import java.util.*

class TaskResultIdAdapter {
    @ToJson
    fun toJson(item: TaskResult.Id): String = item.value.toString()

    @FromJson
    fun fromJson(item: String): TaskResult.Id = TaskResult.Id(UUID.fromString(item))
}
