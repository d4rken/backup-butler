package eu.darken.bb.tasks.core

import android.content.Intent
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.repos.RepoReference
import eu.darken.bb.common.Jsonable
import java.util.*

interface BackupTask : Jsonable {
    val taskName: String
    val id: UUID
    val sources: List<BackupConfig>
    val destinations: List<RepoReference>

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