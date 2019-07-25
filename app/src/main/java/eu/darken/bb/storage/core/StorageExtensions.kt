package eu.darken.bb.storage.core

import android.content.Intent
import android.os.Bundle
import java.util.*

object BackupTaskExtensions {
    internal const val TASKID_KEY = "storage.uuid"
}

fun Intent.putStorageId(uuid: UUID) = apply { putExtra(BackupTaskExtensions.TASKID_KEY, uuid) }

fun Intent.getStorageId(): UUID? = getSerializableExtra(BackupTaskExtensions.TASKID_KEY) as UUID?

fun Bundle.putStorageId(uuid: UUID) = apply { putSerializable(BackupTaskExtensions.TASKID_KEY, uuid) }

fun Bundle.getStorageId(): UUID? = getSerializable(BackupTaskExtensions.TASKID_KEY) as UUID?