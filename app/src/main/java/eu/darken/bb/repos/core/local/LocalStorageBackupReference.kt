package eu.darken.bb.repos.core.local

import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.common.file.SFile
import eu.darken.bb.repos.core.BackupReference
import eu.darken.bb.repos.core.DefaultRevisionConfig

data class LocalStorageBackupReference(
        val path: SFile,
        override val backupConfig: BackupConfig,
        override val revisionConfig: DefaultRevisionConfig
) : BackupReference