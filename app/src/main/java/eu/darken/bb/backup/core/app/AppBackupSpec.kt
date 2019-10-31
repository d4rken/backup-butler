package eu.darken.bb.backup.core.app

import android.content.Context
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.file.APath
import eu.darken.bb.common.tryGetAppLabel


data class AppBackupSpec(
        val packageName: String,
        override val specId: BackupSpec.Id = BackupSpec.Id("pkg-$packageName"),
        override val revisionLimit: Int = 3,
        val backupApk: Boolean,
        val backupData: Boolean,
        val backupCache: Boolean,
        val extraPaths: Set<APath>
) : BackupSpec {

    override fun getLabel(context: Context): String = context.packageManager.tryGetAppLabel(packageName)

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {}
}