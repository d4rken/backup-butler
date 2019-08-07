package eu.darken.bb.task.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.task.core.BackupTaskExtensions.TASKID_KEY

object BackupTaskExtensions {
    internal const val TASKID_KEY = "backuptask.uuid"
}

fun Intent.putTaskId(id: BackupTask.Id) = apply { putExtra(TASKID_KEY, id) }

fun Intent.getTaskId(): BackupTask.Id? = getParcelableExtra(TASKID_KEY) as BackupTask.Id?

fun Bundle.putTaskId(id: BackupTask.Id) = apply { putParcelable(TASKID_KEY, id) }

fun Bundle.getTaskId(): BackupTask.Id? = getParcelable(TASKID_KEY) as BackupTask.Id?