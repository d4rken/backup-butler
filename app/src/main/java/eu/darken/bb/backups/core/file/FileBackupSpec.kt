package eu.darken.bb.backups.core.file

import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupSpec
import eu.darken.bb.common.file.SFile

data class FileBackupSpec(
        val name: String,
        val paths: List<SFile>,
        override val label: String = "files-$name"
) : BackupSpec {

    override val configType: Backup.Type = Backup.Type.FILE

}