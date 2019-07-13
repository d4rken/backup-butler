package eu.darken.bb.backup.backups.app

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.processor.cache.CacheRef

data class AppBackup(
        override val id: BackupId,
        override val backupType: Backup.Type = Backup.Type.APP_BACKUP,
        override val config: Config,
        override val data: MutableMap<String, Collection<CacheRef>> = mutableMapOf()
) : Backup {

    var baseApk: CacheRef
        get() = data["APK_BASE"]!!.first()
        set(value) {
            data["APK_BASE"] = listOf(value)
        }

    var splitApks: Collection<CacheRef>
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
