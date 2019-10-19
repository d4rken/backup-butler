package eu.darken.bb.task.core.results

import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.bb.task.core.Task
import java.util.*

@Entity(tableName = "task_results")
data class StoredResult(
        @PrimaryKey
        override val resultId: Task.Result.Id,
        override val taskId: Task.Id,
        override val label: String,
        override val taskType: Task.Type,
        override val state: Task.Result.State,
        override val startedAt: Date,
        override val duration: Long,
        override val primary: String?,
        override val secondary: String?,
        override val extra: String?,
        override val taskLog: List<String>?
) : Task.Result

