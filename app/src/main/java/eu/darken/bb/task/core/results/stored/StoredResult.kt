package eu.darken.bb.task.core.results.stored

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.TaskResult
import java.util.*

@Keep
@Entity(tableName = "task_results")
data class StoredResult(
        @PrimaryKey
        val id: TaskResult.Id,
        val taskId: Task.Id,
        val label: String,
        val taskType: Task.Type,
        val state: TaskResult.State,
        val startedAt: Date,
        val duration: Long,
        val primary: String?,
        val secondary: String?,
        val extra: String?
)
