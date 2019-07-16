package eu.darken.bb.backup.backups.app

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.tmp.TmpRef

data class AppBackup(
        private val packageName: String,
        override val id: BackupId,
        override val backupType: Backup.Type = Backup.Type.APP_BACKUP,
        override val config: Config,
        override val data: MutableMap<String, Collection<TmpRef>> = mutableMapOf()
) : Backup {

    override val name: String
        get() = "pkg-$packageName"

    var baseApk: TmpRef
        get() = data["APK_BASE"]!!.first()
        set(value) {
            data["APK_BASE"] = listOf(value)
        }

    var splitApks: Collection<TmpRef>
        get() = data["APK_SPLIT"]!!
        set(value) {
            data["APK_SPLIT"] = value
        }

    data class Config(
            val packageName: String
    ) : Backup.Config {

        override val configType: Backup.Type = Backup.Type.APP_BACKUP

    }
}
