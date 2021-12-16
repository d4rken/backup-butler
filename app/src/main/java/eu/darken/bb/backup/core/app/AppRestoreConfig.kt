package eu.darken.bb.backup.core.app

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore

@Keep
@JsonClass(generateAdapter = true)
data class AppRestoreConfig(
    val skipExistingApps: Boolean,
    val restoreApk: Boolean,
    val restoreData: Boolean,
    val restoreCache: Boolean,
    val overwriteExisting: Boolean
) : Restore.Config {

    override var restoreType: Backup.Type
        get() = Backup.Type.APP
        set(value) {}

}