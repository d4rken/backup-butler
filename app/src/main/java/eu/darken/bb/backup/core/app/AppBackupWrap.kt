package eu.darken.bb.backup.core.app

import androidx.annotation.StringRes
import eu.darken.bb.R
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
        get() = (data[DataType.APK_BASE.key] ?: emptyList()).single()
        set(value) {
            data[DataType.APK_BASE.key] = listOf(value)
        }

    var splitApks: Collection<MMRef>
        get() = data[DataType.APK_SPLIT.key] ?: emptyList()
        set(value) {
            data[DataType.APK_SPLIT.key] = value
        }

    var extraPaths: Collection<MMRef>
        get() = data[DataType.APK_SPLIT.key] ?: emptyList()
        set(value) {
            data[DataType.APK_SPLIT.key] = value
        }

    fun getDataType(type: DataType, user: Int = 0): Collection<MMRef> {
        return data[type.forUser(user)] ?: emptyList()
    }

    fun putDataType(type: DataType, items: Collection<MMRef>, user: Int = 0) {
        data[type.forUser(user)] = items
    }

    enum class DataType(
        val key: String,
        @StringRes val labelRes: Int
    ) {
        APK_BASE("APK_BASE", R.string.app_apk_base_label),
        APK_SPLIT("APK_SPLIT", R.string.app_apk_split_label),
        DATA_PRIVATE_PRIMARY("DATA_PRIVATE_PRIMARY", R.string.app_data_private_primary_label),
        DATA_PUBLIC_PRIMARY("DATA_PUBLIC_PRIMARY", R.string.app_data_public_primary_label),
        DATA_PUBLIC_SECONDARY("DATA_PUBLIC_SECONDARY", R.string.app_data_public_secondary_label),
        DATA_SDCARD_PRIMARY("DATA_SDCARD_PRIMARY", R.string.app_data_sdcard_primary),
        DATA_SDCARD_SECONDARY("DATA_SDCARD_SECONDARY", R.string.app_data_sdcard_secondary),
        CACHE_PRIVATE_PRIMARY("CACHE_PRIVATE_PRIMARY", R.string.app_cache_private_primary_label),
        CACHE_PUBLIC_PRIMARY("CACHE_PUBLIC_PRIMARY", R.string.app_cache_public_primary_label),
        CACHE_PUBLIC_SECONDARY("CACHE_PUBLIC_SECONDARY", R.string.app_cache_public_secondary_label),
        CACHE_SDCARD_PRIMARY("CACHE_SDCARD_PRIMARY", R.string.app_cache_sdcard_primary),
        CACHE_SDCARD_SECONDARY("CACHE_SDCARD_SECONDARY", R.string.app_cache_sdcard_secondary),
        EXTRA_PATHS("EXTRA_PATHS", R.string.app_custom_data);

        fun forUser(user: Int): String = "${key};userId=$user"
    }
}
