package eu.darken.bb.task.core.results.stored

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.bb.task.core.results.TaskResult

@Keep
@Entity(tableName = "task_log_events")
data class StoredLogEvent(
        val subResultId: TaskResult.SubResult.Id,
        val type: String,
        val description: String
) {

    @PrimaryKey(autoGenerate = true) var id: Long? = null
}