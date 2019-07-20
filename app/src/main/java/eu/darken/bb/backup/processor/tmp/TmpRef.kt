package eu.darken.bb.backup.processor.tmp

import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.common.file.SFile
import java.util.*

data class TmpRef(
        val refId: TmpRefId = TmpRefId(),
        val backupId: BackupId,
        val type: TmpType,
        val file: SFile
) {
    var originalPath: SFile? = null
}

inline class TmpRefId(val id: UUID = UUID.randomUUID()) {
    override fun toString(): String = id.toString()
}