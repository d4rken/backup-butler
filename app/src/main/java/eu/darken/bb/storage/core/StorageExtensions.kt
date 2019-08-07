package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle

object BackupTaskExtensions {
    internal const val STORAGEID_KEY = "storage.uuid"
}

fun Intent.putStorageId(id: BackupStorage.Id) = apply { putExtra(BackupTaskExtensions.STORAGEID_KEY, id) }

fun Intent.getStorageId(): BackupStorage.Id? = getParcelableExtra(BackupTaskExtensions.STORAGEID_KEY) as BackupStorage.Id?

fun Bundle.putStorageId(id: BackupStorage.Id) = apply { putParcelable(BackupTaskExtensions.STORAGEID_KEY, id) }

fun Bundle.getStorageId(): BackupStorage.Id? = getParcelable(BackupTaskExtensions.STORAGEID_KEY) as BackupStorage.Id?