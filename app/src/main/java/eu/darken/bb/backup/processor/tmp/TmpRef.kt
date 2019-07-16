package eu.darken.bb.backup.processor.tmp

import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.common.file.SFile

data class TmpRef(
        val refId: RefId = RefId(),
        val backupId: BackupId,
        val type: TmpType,
        val file: SFile
) {
    var originalPath: SFile? = null
}