package eu.darken.bb.storage.core

import eu.darken.bb.backups.core.BackupConfig

interface BackupReference {
    val backupConfig: BackupConfig
    val revisionConfig: RevisionConfig
}