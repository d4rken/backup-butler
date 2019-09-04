package eu.darken.bb.task.core.restore

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task

data class SimpleRestoreTask(
        override val taskName: String,
        override val taskId: Task.Id,
        val restoreConfigs: Collection<Restore.Config> = emptySet(),
        val targetStorage: Collection<Storage.Id> = emptySet(),
        val targetBackupSpec: Collection<BackupSpec.Target> = emptySet(),
        val targetBackup: Collection<Backup.Target> = emptySet()
) : Task.Restore {

    override val taskType: Task.Type = Task.Type.RESTORE_SIMPLE

    override fun getDescription(context: Context): String {
        return context.getString(R.string.task_restore_simple_description, targetStorage.size, targetBackupSpec.size, targetBackup.size)
    }

}