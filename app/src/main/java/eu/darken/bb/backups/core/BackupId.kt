package eu.darken.bb.backups.core

import java.util.*

class BackupId(val id: UUID = UUID.randomUUID()) {
    override fun toString(): String = id.toString()
}