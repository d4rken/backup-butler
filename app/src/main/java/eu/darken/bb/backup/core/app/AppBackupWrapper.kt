package eu.darken.bb.backup.core.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BaseBackupWrapper
import eu.darken.bb.processor.core.mm.MMRef

class AppBackupWrapper
    : BaseBackupWrapper<AppBackupSpec> {
    constructor(backup: Backup.Unit) : super(backup)

    constructor(config: AppBackupSpec, backupId: Backup.Id)
            : super(backupId, config)

    override fun buildMeta(backupId: Backup.Id): Backup.MetaData {
        return AppBackupMetaData(backupId)
    }

    val packageName: String
        get() = backupConfig.packageName

    var baseApk: MMRef
        get() = data["APK_BASE"]!!.first()
        set(value) {
            data["APK_BASE"] = mutableListOf(value)
        }

    var splitApks: MutableCollection<MMRef>
        get() = data["APK_SPLIT"]!!
        set(value) {
            data["APK_SPLIT"] = value
        }

    var dataPrivate: MutableCollection<MMRef>
        get() = data["DATA_PRIVATE"]!!
        set(value) {
            data["DATA_PRIVATE"] = value
        }

}
