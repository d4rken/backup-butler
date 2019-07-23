package eu.darken.bb.tasks.core

import android.content.Intent
import android.os.Bundle
import eu.darken.bb.tasks.core.BackupTaskExtensions.TASKID_KEY
import java.util.*

object BackupTaskExtensions {
    internal const val TASKID_KEY = "backuptask.uuid"
}

fun Intent.putTaskId(uuid: UUID) = apply { putExtra(TASKID_KEY, uuid) }

fun Intent.getTaskId(): UUID? = getSerializableExtra(TASKID_KEY) as UUID?

fun Bundle.putTaskId(uuid: UUID) = apply { putSerializable(TASKID_KEY, uuid) }

fun Bundle.getTaskId(): UUID? = getSerializable(TASKID_KEY) as UUID?