package eu.darken.bb.storage.core

import eu.darken.bb.backups.BackupConfig

interface BackupReference {
    val backupConfig: BackupConfig
    val revisionConfig: RevisionConfig
}