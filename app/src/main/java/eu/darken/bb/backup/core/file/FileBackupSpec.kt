package eu.darken.bb.backup.core.file

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.SFile

data class FileBackupSpec(
        val name: String,
        val paths: List<SFile>,
        override val label: String = "files-$name"
) : BackupSpec {

    override val configType: Backup.Type = Backup.Type.FILE

}