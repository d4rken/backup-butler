package eu.darken.bb.backup.processor

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.tasks.core.BackupTask
import java.util.*

data class DefaultBackupTask(
        override val id: UUID,
        override val sources: List<Backup.Config>,
        override val destinations: List<BackupRepo.RepoRef>
) : BackupTask {


    data class Result(
            override val taskID: String,
            override val state: BackupTask.Result.State,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result
}