package eu.darken.bb.task.core.results.stored

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.darken.bb.task.core.results.TaskResult
import java.util.*

@Keep
@Entity(tableName = "task_subresults")
data class StoredSubResult(
        @PrimaryKey
        val id: TaskResult.SubResult.Id,
        val resultId: TaskResult.Id,
        val startedAt: Date,
        val duration: Long,
        val label: String,
        val state: TaskResult.State,
        val primary: String?,
        val secondary: String?,
        val extra: String?
)
