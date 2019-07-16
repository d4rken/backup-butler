package eu.darken.bb.backup.backups.file

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.tmp.TmpRef
import eu.darken.bb.common.CheckSummer
import eu.darken.bb.common.file.SFile

class FileBackup(
        override val config: Config,
        override val backupType: Backup.Type = Backup.Type.FILE,
        override val id: BackupId,
        override val data: MutableMap<String, Collection<TmpRef>> = mutableMapOf()
) : Backup {
    private val folder = config.files.first()
    private val folderHash by lazy { CheckSummer.calculate(folder.name, CheckSummer.Type.MD5) }

    override val name: String
        get() = "folder-${folder.name}-$folderHash"

    data class Config(val files: List<SFile>) : Backup.Config {
        override val configType: Backup.Type
            get() = Backup.Type.FILE

    }
}
