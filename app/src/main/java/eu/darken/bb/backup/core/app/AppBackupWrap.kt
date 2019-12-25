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
        get() = (data[DKEY_APK_BASE] ?: emptyList()).single()
        set(value) {
            data[DKEY_APK_BASE] = listOf(value)
        }

    var splitApks: Collection<MMRef>
        get() = data[DKEY_APK_SPLIT] ?: emptyList()
        set(value) {
            data[DKEY_APK_SPLIT] = value
        }

    fun getDataType(type: DataType, user: Int = 0): Collection<MMRef> {
        return data[type.forUser(user)] ?: emptyList()
    }

    fun putDataType(type: DataType, items: Collection<MMRef>, user: Int = 0) {
        data[type.forUser(user)] = items
    }

    enum class DataType(val key: String) {
        DATA_PRIVATE_PRIMARY("DATA_PRIVATE_PRIMARY"),
        DATA_PUBLIC_PRIMARY("DATA_PUBLIC_PRIMARY"),
        DATA_PUBLIC_SECONDARY("DATA_PUBLIC_SECONDARY"),
        CACHE_PRIVATE_PRIMARY("CACHE_PRIVATE_PRIMARY"),
        CACHE_PUBLIC_PRIMARY("CACHE_PUBLIC_PRIMARY"),
        CACHE_PUBLIC_SECONDARY("CACHE_PUBLIC_SECONDARY");

        fun forUser(user: Int): String = "$key:$user"
    }

    companion object {
        const val DKEY_APK_BASE = "APK_BASE"
        const val DKEY_APK_SPLIT = "APK_SPLIT"
    }
}
