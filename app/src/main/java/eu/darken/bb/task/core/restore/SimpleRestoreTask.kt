package eu.darken.bb.task.core.restore

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task

data class SimpleRestoreTask(
        override val taskId: Task.Id,
        override val taskName: String = "",
        val restoreConfigs: Set<Restore.Config> = emptySet(),
        val targetStorages: Set<Storage.Id> = emptySet(),
        val targetBackupSpec: Set<BackupSpec.Target> = emptySet(),
        val targetBackup: Set<Backup.Target> = emptySet()
) : Task.Restore {

    override var taskType: Task.Type
        get() = Task.Type.RESTORE_SIMPLE
        set(value) {}

    override fun getDescription(context: Context): String {
        return context.getString(R.string.task_restore_simple_description, targetStorages.size, targetBackupSpec.size, targetBackup.size)
    }

}