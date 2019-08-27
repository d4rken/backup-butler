package eu.darken.bb.backup.core.files.legacy

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.CheckSummer
import eu.darken.bb.common.file.SFile

data class LegacyFilesBackupSpec(
        val label: String,
        val path: SFile,
        override val revisionLimit: Int = 3
) : BackupSpec {

    override var specId: BackupSpec.Id
        get() = BackupSpec.Id(CheckSummer.calculate(label + path.path, CheckSummer.Type.MD5))
        set(value) {}

    override fun getLabel(context: Context): String = label

    override val backupType: Backup.Type = Backup.Type.FILES

}