package eu.darken.bb.history

import eu.darken.bb.task.core.Task

data class StoredResult(
        val taskId: String,
        val startTime: Long,
        val endTime: Long,
        val primary: String,
        val secondary: String,
        val state: Task.Result.State
)
