package eu.darken.bb.backup.core.file

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.SFile

data class FileBackupSpec(
        val name: String,
        val path: SFile,
        override val specId: BackupSpec.Id = BackupSpec.Id("files-$name"),
        override val revisionLimit: Int = 3
) : BackupSpec {

    override fun getLabel(context: Context): String = name

    override val backupType: Backup.Type = Backup.Type.FILE

}