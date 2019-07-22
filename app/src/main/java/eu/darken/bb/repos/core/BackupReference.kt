package eu.darken.bb.repos.core

import eu.darken.bb.backup.backups.BackupConfig

interface BackupReference {
    val backupConfig: BackupConfig
    val revisionConfig: RevisionConfig
}