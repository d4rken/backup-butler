package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.tryGetAppLabel
import eu.darken.bb.processor.core.mm.MMRef


data class AppBackupSpec(
        val packageName: String,
        override val specId: BackupSpec.Id = BackupSpec.Id("pkg-$packageName"),
        override val revisionLimit: Int = 3
) : BackupSpec {
    override fun getLabel(context: Context): String = context.packageManager.tryGetAppLabel(packageName)

    override val backupType: Backup.Type = Backup.Type.APP

    override fun getContentEntryLabel(props: MMRef.Props): String {
        TODO("not implemented")
    }
}