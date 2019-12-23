package eu.darken.bb.task.core.results.stored

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.task.core.results.TaskResult

@Keep
@Entity(tableName = "task_events")
data class StoredIOEvent(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val subResultId: TaskResult.SubResult.Id,
        val timestamp: Long,
        val type: Type,
        val path: String,
        val pathObject: APath
) {
    enum class Type {
        DELETE,
        WRITE
    }
}