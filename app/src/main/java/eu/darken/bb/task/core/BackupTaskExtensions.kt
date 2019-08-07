package eu.darken.bb.task.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.task.core.BackupTaskExtensions.TASKID_KEY

object BackupTaskExtensions {
    internal const val TASKID_KEY = "backuptask.uuid"
}

fun Intent.putTaskId(id: Task.Id) = apply { putExtra(TASKID_KEY, id) }

fun Intent.getTaskId(): Task.Id? = getParcelableExtra(TASKID_KEY) as Task.Id?

fun Bundle.putTaskId(id: Task.Id) = apply { putParcelable(TASKID_KEY, id) }

fun Bundle.getTaskId(): Task.Id? = getParcelable(TASKID_KEY) as Task.Id?