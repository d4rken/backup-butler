package eu.darken.bb.task.core.results

import eu.darken.bb.task.core.Task
import java.util.*

data class GlimpsedTaskResult(
    override val resultId: TaskResult.Id,
    override val taskId: Task.Id,
    override val taskType: Task.Type,
    override val label: String,
    override val startedAt: Date,
    override val duration: Long,
    override val state: TaskResult.State,
    override val primary: String?,
    override val secondary: String?,
    override val extra: String?
) : TaskResult {
    override val subResults: List<TaskResult.SubResult>
        get() {
            throw UnsupportedOperationException()
        }
}