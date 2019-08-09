package eu.darken.bb.backup.core.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BaseBackupBuilder
import eu.darken.bb.processor.tmp.TmpRef

class AppBackupBuilder : BaseBackupBuilder<AppBackupSpec> {
    constructor(backup: Backup) : super(backup)

    constructor(config: AppBackupSpec, backupId: Backup.Id)
            : super(config, backupId)

    val packageName: String
        get() = backupConfig.packageName

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
