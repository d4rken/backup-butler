package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec

object StorageExtensions {
    internal const val STORAGEID_KEY = "storage.id"
    internal const val BACKUPID_KEY = "backup.id"
    internal const val BACKUPSPECID_KEY = "backupspec.id"
}

fun Intent.putStorageId(id: Storage.Id) = apply { putExtra(StorageExtensions.STORAGEID_KEY, id) }
fun Intent.getStorageId(): Storage.Id? = getParcelableExtra(StorageExtensions.STORAGEID_KEY) as Storage.Id?
fun Bundle.putStorageId(id: Storage.Id) = apply { putParcelable(StorageExtensions.STORAGEID_KEY, id) }
fun Bundle.getStorageId(): Storage.Id? = getParcelable(StorageExtensions.STORAGEID_KEY) as Storage.Id?

fun Intent.putBackupSpecId(id: BackupSpec.Id) = apply { putExtra(StorageExtensions.BACKUPSPECID_KEY, id) }
fun Intent.getBackupSpecId(): BackupSpec.Id? = getParcelableExtra(StorageExtensions.BACKUPSPECID_KEY) as BackupSpec.Id?
fun Bundle.putBackupSpecId(id: BackupSpec.Id) = apply { putParcelable(StorageExtensions.BACKUPSPECID_KEY, id) }
fun Bundle.getBackupSpecId(): BackupSpec.Id? = getParcelable(StorageExtensions.BACKUPSPECID_KEY) as BackupSpec.Id?

fun Intent.putBackupId(id: Backup.Id) = apply { putExtra(StorageExtensions.BACKUPID_KEY, id) }
fun Intent.getBackupId(): Backup.Id? = getParcelableExtra(StorageExtensions.BACKUPID_KEY) as Backup.Id?
fun Bundle.putBackupId(id: Backup.Id) = apply { putParcelable(StorageExtensions.BACKUPID_KEY, id) }
fun Bundle.getBackupId(): Backup.Id? = getParcelable(StorageExtensions.BACKUPID_KEY) as Backup.Id?