package eu.darken.bb.backup.core

import java.util.*

data class BackupId(val id: UUID = UUID.randomUUID()) {
    override fun toString(): String = id.toString()
}