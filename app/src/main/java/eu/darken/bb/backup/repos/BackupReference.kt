package eu.darken.bb.backup.repos

import eu.darken.bb.backup.backups.BackupConfig

interface BackupReference {
    val backupConfig: BackupConfig
    val revisionConfig: RevisionConfig
}