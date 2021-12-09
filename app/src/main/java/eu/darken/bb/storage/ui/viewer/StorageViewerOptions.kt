package eu.darken.bb.storage.ui.viewer

import android.os.Parcelable
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import kotlinx.parcelize.Parcelize

@Parcelize
data class StorageViewerOptions(
    val storageId: Storage.Id,
    val specId: BackupSpec.Id? = null,
    val taskId: Task.Id? = null,
    val backupTypeFilter: Set<Backup.Type>? = null
) : Parcelable
