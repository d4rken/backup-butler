package eu.darken.bb.tasks.core

import android.content.Intent
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.common.Jsonable
import java.util.*

interface BackupTask : Jsonable {
    val id: UUID

    val sources: List<Backup.Config>

    val destinations: List<BackupRepo.Config>

    interface Result {
        enum class State {
            SUCCESS, ERROR
        }

        val taskID: String
        val state: State
        val primary: String?
        val secondary: String?
    }
}

fun Intent.addTaskId(uuid: UUID) = apply { putExtra("backuptask.uuid", uuid) }

fun Intent.getTaskId(): UUID? = getSerializableExtra("backuptask.uuid") as UUID?