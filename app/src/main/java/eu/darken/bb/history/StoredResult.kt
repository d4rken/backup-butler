package eu.darken.bb.history

import eu.darken.bb.task.core.BackupTask

data class StoredResult(
        val taskId: String,
        val startTime: Long,
        val endTime: Long,
        val primary: String,
        val secondary: String,
        val state: BackupTask.Result.State
)
