package eu.darken.bb.task.core.restore

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task

data class SimpleRestoreTask(
        override val taskName: String,
        override val taskId: Task.Id,
        val restoreConfigs: Collection<Restore.Config> = emptySet(),
        val storageIds: Collection<Storage.Id> = emptySet(),
        val backupSpecIds: Collection<BackupSpec.Id> = emptySet(),
        val backupIds: Collection<Backup.Id> = emptySet()
) : Task.Restore {

    override val taskType: Task.Type = Task.Type.RESTORE_SIMPLE

    override fun getDescription(context: Context): String {
        return ""
    }

}