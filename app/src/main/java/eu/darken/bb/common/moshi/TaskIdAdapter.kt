package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.task.core.BackupTask
import java.util.*

class TaskIdAdapter {
    @ToJson
    fun toJson(item: BackupTask.Id): String = item.id.toString()

    @FromJson
    fun fromJson(item: String): BackupTask.Id = BackupTask.Id(UUID.fromString(item))
}
