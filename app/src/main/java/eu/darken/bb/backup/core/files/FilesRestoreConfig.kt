package eu.darken.bb.backup.core.files

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.file.APath

@Keep
@JsonClass(generateAdapter = true)
data class FilesRestoreConfig(
        val replaceFiles: Boolean = false,
        val restorePath: APath? = null
) : Restore.Config {

    override var restoreType: Backup.Type
        get() = Backup.Type.FILES
        set(value) {}

}