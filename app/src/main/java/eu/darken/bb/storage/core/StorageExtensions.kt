package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle

object BackupTaskExtensions {
    internal const val STORAGEID_KEY = "storage.uuid"
}

fun Intent.putStorageId(id: Storage.Id) = apply { putExtra(BackupTaskExtensions.STORAGEID_KEY, id) }

fun Intent.getStorageId(): Storage.Id? = getParcelableExtra(BackupTaskExtensions.STORAGEID_KEY) as Storage.Id?

fun Bundle.putStorageId(id: Storage.Id) = apply { putParcelable(BackupTaskExtensions.STORAGEID_KEY, id) }

fun Bundle.getStorageId(): Storage.Id? = getParcelable(BackupTaskExtensions.STORAGEID_KEY) as Storage.Id?