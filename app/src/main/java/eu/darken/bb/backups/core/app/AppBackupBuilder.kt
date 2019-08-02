package eu.darken.bb.backups.core.app

import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupId
import eu.darken.bb.backups.core.BaseBackupBuilder
import eu.darken.bb.processor.tmp.TmpRef

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
