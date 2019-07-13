package eu.darken.bb.backup.processor.cache

import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.common.file.SFile
import java.util.*

data class CacheRef(
        val refId: UUID,
        val backupId: BackupId,
        val file: SFile
)