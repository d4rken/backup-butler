package eu.darken.bb.backup.backups.app

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupId
import eu.darken.bb.backup.backups.BaseBackupBuilder
import eu.darken.bb.backup.processor.tmp.TmpRef

class AppBackupBuilder : BaseBackupBuilder<AppBackupConfig> {
    constructor(backup: Backup) : super(backup)

    constructor(config: AppBackupConfig, backupId: BackupId) : super(config, backupId)

    var packageName: String
        get() = backupConfig.packageName
        set(value) {
            backupConfig = backupConfig.copy(packageName = value)
        }

    var baseApk: TmpRef
        get() = data["APK_BASE"]!!.first()
        set(value) {
            data["APK_BASE"] = mutableListOf(value)
        }

    var splitApks: MutableCollection<TmpRef>
        get() = data["APK_SPLIT"]!!
        set(value) {
            data["APK_SPLIT"] = value
        }

}
