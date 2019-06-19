package eu.darken.bb.backup.processor

import eu.darken.bb.backup.BackupTask
import eu.darken.bb.backup.Destination
import eu.darken.bb.backup.Source
import java.util.*

data class DefaultBackupTask(
        override val id: UUID,
        override val sources: List<Source.Config>,
        override val destinations: List<Destination.Config>
) : BackupTask {


    data class Result(
            override val taskID: String,
            override val state: BackupTask.Result.State,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : BackupTask.Result
}