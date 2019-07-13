package eu.darken.bb.backup.backups.file

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.cache.CacheRef
import eu.darken.bb.common.file.SFile

class FileBackup(
        override val config: Config,
        override val backupType: Backup.Type = Backup.Type.FILE,
        override val id: BackupId,
        override val data: MutableMap<String, Collection<CacheRef>> = mutableMapOf()
) : Backup {

    data class Config(val files: List<SFile>) : Backup.Config {
        override val configType: Backup.Type
            get() = Backup.Type.FILE

    }
}
