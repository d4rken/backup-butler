package eu.darken.bb.processor.core.tmp

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.SFile
import java.util.*

data class TmpRef(
        val refId: UUID,
        val backupId: Backup.Id,
        val type: Type,
        val file: SFile
) {
    var originalPath: SFile? = null

    enum class Type {
        FILE, DIRECTORY
    }
}