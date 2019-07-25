package eu.darken.bb.backups.file

import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupConfig
import eu.darken.bb.common.file.SFile

data class FileBackupConfig(
        val name: String,
        val paths: List<SFile>,
        override val label: String = "files-$name"
) : BackupConfig {

    override val configType: Backup.Type = Backup.Type.FILE

}