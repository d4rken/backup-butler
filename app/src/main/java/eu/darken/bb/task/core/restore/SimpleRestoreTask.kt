package eu.darken.bb.task.core.restore

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.task.core.Task

data class SimpleRestoreTask(
        override val taskId: Task.Id,
        override val label: String = "",
        val defaultConfigs: Map<Backup.Type, Restore.Config> = emptyMap(),
        val customConfigs: Map<Backup.Id, Restore.Config> = emptyMap(),
        val backupTargets: Set<Backup.Target> = emptySet()
) : Task.Restore {

    // TODO test serialization

    override var taskType: Task.Type
        get() = Task.Type.RESTORE_SIMPLE
        set(value) {}

    override fun getDescription(context: Context): String {
        return context.getString(R.string.task_restore_simple_description, backupTargets.size)
    }

}