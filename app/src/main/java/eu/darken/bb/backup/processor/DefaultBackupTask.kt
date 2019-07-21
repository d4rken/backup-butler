package eu.darken.bb.backup.processor

import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.tasks.core.BackupTask
import java.util.*

data class DefaultBackupTask(
        override val id: UUID,
        override val sources: List<BackupConfig>,
        override val destinations: List<BackupRepo.RepoReference>
) : BackupTask {


    data class Result(
            override val taskID: String,
            override val state: BackupTask.Result.State,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result
}