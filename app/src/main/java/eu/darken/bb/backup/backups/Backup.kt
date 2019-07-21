package eu.darken.bb.backup.backups

import androidx.annotation.Keep
import eu.darken.bb.backup.processor.tmp.TmpRef

data class Backup(
        val id: BackupId,
        val backupType: Type,
        val config: BackupConfig,
        val data: Map<String, Collection<TmpRef>>
) {

    @Keep
    enum class Type {
        APP_BACKUP, FILE
    }
}

