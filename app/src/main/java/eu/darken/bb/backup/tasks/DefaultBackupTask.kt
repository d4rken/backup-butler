package eu.darken.bb.backup.tasks

import eu.darken.bb.backup.BackupTask
import java.util.*

data class DefaultBackupTask(
        override val id: UUID,
        override val sources: List<BackupTask.Source>,
        override val destinations: List<BackupTask.Destination>
) : BackupTask