package eu.darken.bb.backup.core.files

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.CheckSummer
import eu.darken.bb.common.file.APath

data class FilesBackupSpec(
        val path: APath,
        val label: String,
        override val revisionLimit: Int = 3
) : BackupSpec {

    override var specId: BackupSpec.Id
        get() = BackupSpec.Id(CheckSummer.calculate(label + path.path, CheckSummer.Type.MD5))
        set(value) {}

    override fun getLabel(context: Context): String = label

    override var backupType: Backup.Type
        get() = Backup.Type.FILES
        set(value) {}
}