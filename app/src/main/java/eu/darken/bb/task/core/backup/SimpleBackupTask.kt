package eu.darken.bb.task.core.backup

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task

@Keep
@JsonClass(generateAdapter = true)
data class SimpleBackupTask(
        override val taskId: Task.Id,
        override val label: String = "",
        override val sources: Set<Generator.Id> = emptySet(),
        override val destinations: Set<Storage.Id> = emptySet(),
        override val isOneTimeTask: Boolean = false
) : Task.Backup {

    override var taskType: Task.Type
        get() = Task.Type.BACKUP_SIMPLE
        set(value) {}

    override fun getDescription(context: Context): String {
        return context.getString(R.string.task_type_backupsimple_description, sources.size, destinations.size)
    }

}