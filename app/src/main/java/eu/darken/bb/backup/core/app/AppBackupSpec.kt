package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec


data class AppBackupSpec(
        val packageName: String,
        override val specId: BackupSpec.Id = BackupSpec.Id("pkg-$packageName")
) : BackupSpec {

    override fun getLabel(context: Context): String = packageName

    override val backupType: Backup.Type = Backup.Type.APP
}