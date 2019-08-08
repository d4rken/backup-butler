package eu.darken.bb.storage.core

import eu.darken.bb.backup.core.BackupSpec

interface BackupReference {
    val backupSpec: BackupSpec
    val versioning: Versioning
}