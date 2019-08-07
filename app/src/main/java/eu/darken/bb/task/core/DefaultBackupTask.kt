package eu.darken.bb.task.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.storage.core.BackupStorage

data class DefaultBackupTask(
        override val taskName: String,
        override val taskId: BackupTask.Id,
        override val sources: Set<Generator.Id>,
        override val destinations: Set<BackupStorage.Id>
) : BackupTask {

    override fun getDescription(context: Context): String {
        return context.getString(R.string.default_backuptask_description_x_sources_x_destinations, sources.size, destinations.size)
    }

    data class Result(
            override val taskID: BackupTask.Id,
            override val state: BackupTask.Result.State,
            override val error: Exception? = null,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result {
        constructor(taskId: BackupTask.Id, error: Exception)
                : this(taskID = taskId, state = BackupTask.Result.State.ERROR, error = error)
    }

}