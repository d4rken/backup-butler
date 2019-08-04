package eu.darken.bb.storage.core

import eu.darken.bb.backups.core.BackupSpec

interface BackupReference {
    val backupSpec: BackupSpec
    val revisionConfig: RevisionConfig
}