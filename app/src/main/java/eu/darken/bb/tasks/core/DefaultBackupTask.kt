package eu.darken.bb.tasks.core

import android.content.Context
import eu.darken.bb.R
import java.util.*

data class DefaultBackupTask(
        override val taskName: String,
        override val taskId: UUID,
        override val sources: Set<UUID>,
        override val destinations: Set<UUID>
) : BackupTask {

    override fun getDescription(context: Context): String {
        return context.getString(R.string.default_backuptask_description_x_sources_x_destinations, sources.size, destinations.size)
    }

    data class Result(
            override val taskID: UUID,
            override val state: BackupTask.Result.State,
            override val error: Exception? = null,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result {
        constructor(taskId: UUID, error: Exception)
                : this(taskID = taskId, state = BackupTask.Result.State.ERROR, error = error)
    }

}