package eu.darken.bb.backup.core.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupWrap
import eu.darken.bb.processor.core.mm.MMRef

class AppBackupWrap
    : BackupWrap<AppBackupSpec> {
    constructor(backup: Backup.Unit) : super(backup)

    constructor(config: AppBackupSpec, backupId: Backup.Id)
            : super(backupId, config)

    override fun buildMeta(backupId: Backup.Id): Backup.MetaData {
        return AppBackupMetaData(backupId)
    }

    val packageName: String
        get() = backupConfig.packageName

    var baseApk: MMRef
        get() = getType(Type.APK_BASE).single()
        set(value) = putType(Type.APK_BASE, listOf(value))

    var splitApks: Collection<MMRef>
        get() = getType(Type.APK_SPLIT)
        set(value) = putType(Type.APK_SPLIT, value)

    fun getType(type: Type): Collection<MMRef> {
        return data[type.key] ?: emptyList()
    }

    fun putType(type: Type, items: Collection<MMRef>) {
        data[type.key] = items
    }

    enum class Type(val key: String) {
        APK_BASE("APK_BASE"),
        APK_SPLIT("APK_SPLIT"),
        DATA_PRIVATE_PRIMARY("DATA_PRIVATE_PRIMARY"),
        DATA_PUBLIC_PRIMARY("DATA_PUBLIC_PRIMARY"),
        CACHE_PRIVATE_PRIMARY("CACHE_PRIVATE_PRIMARY"),
        CACHE_PUBLIC_PRIMARY("CACHE_PUBLIC_PRIMARY")
    }
}
