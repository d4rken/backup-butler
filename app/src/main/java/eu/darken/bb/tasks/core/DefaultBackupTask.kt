package eu.darken.bb.tasks.core

import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.repos.core.RepoRef
import java.util.*

data class DefaultBackupTask(
        override val taskName: String,
        override val taskId: UUID,
        override val sources: List<BackupConfig>,
        override val destinations: List<RepoRef>
) : BackupTask {

    data class Result(
            override val taskID: String,
            override val state: BackupTask.Result.State,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result

}