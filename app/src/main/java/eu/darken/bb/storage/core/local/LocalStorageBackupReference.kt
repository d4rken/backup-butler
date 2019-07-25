package eu.darken.bb.storage.core.local

import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.common.file.SFile
import eu.darken.bb.storage.core.BackupReference
import eu.darken.bb.storage.core.DefaultRevisionConfig

data class LocalStorageBackupReference(
        val path: SFile,
        override val backupConfig: BackupConfig,
        override val revisionConfig: DefaultRevisionConfig
) : BackupReference