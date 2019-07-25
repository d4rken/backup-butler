package eu.darken.bb.backups

import java.util.*

class BackupId(val id: UUID = UUID.randomUUID()) {
    override fun toString(): String = id.toString()
}