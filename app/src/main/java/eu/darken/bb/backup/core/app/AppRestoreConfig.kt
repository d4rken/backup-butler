package eu.darken.bb.backup.core.app

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore

@Keep
@JsonClass(generateAdapter = true)
data class AppRestoreConfig(
        val skipExistingApps: Boolean = false,
        val restoreApk: Boolean = true,
        val restoreData: Boolean = true,
        val restoreCache: Boolean = false
) : Restore.Config {

    override var restoreType: Backup.Type
        get() = Backup.Type.APP
        set(value) {}

}