package eu.darken.bb.backup.processor.tmp

import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.common.file.SFile
import java.util.*

data class TmpRef(
        val refId: UUID,
        val backupId: BackupId,
        val type: Type,
        val file: SFile
) {
    var originalPath: SFile? = null

    enum class Type {
        FILE, DIRECTORY
    }
}