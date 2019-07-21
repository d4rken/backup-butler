package eu.darken.bb.backup.repos.local

import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.backup.repos.BackupReference
import eu.darken.bb.backup.repos.DefaultRevisionConfig
import eu.darken.bb.common.file.SFile

data class LocalStorageBackupReference(
        val path: SFile,
        override val backupConfig: BackupConfig,
        override val revisionConfig: DefaultRevisionConfig
) : BackupReference