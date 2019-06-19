package eu.darken.bb.backup

import eu.darken.bb.common.Jsonable
import java.util.*

interface BackupTask : Jsonable {
    val id: UUID

    val sources: List<Source.Config>

    val destinations: List<Destination.Config>

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