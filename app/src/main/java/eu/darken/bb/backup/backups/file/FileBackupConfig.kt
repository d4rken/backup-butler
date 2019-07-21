package eu.darken.bb.backup.backups.file

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupConfig
import eu.darken.bb.common.file.SFile

data class FileBackupConfig(
        val name: String,
        val paths: List<SFile>,
        override val backupName: String = "files-$name"
) : BackupConfig {

    override val configType: Backup.Type = Backup.Type.FILE

}