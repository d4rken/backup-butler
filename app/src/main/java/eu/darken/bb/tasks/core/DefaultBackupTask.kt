package eu.darken.bb.tasks.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backups.core.BackupConfig
import eu.darken.bb.storage.core.StorageRef
import java.util.*

data class DefaultBackupTask(
        override val taskName: String,
        override val taskId: UUID,
        override val sources: Set<BackupConfig>,
        override val destinations: Set<StorageRef>
) : BackupTask {

    override fun getDescription(context: Context): String {
        return context.getString(R.string.default_backuptask_description_x_sources_x_destinations, sources.size, destinations.size)
    }

    data class Result(
            override val taskID: String,
            override val state: BackupTask.Result.State,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result

}