package eu.darken.bb.backup.core.app

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.tryGetAppLabel

@Keep
@JsonClass(generateAdapter = true)
data class AppBackupSpec(
        val packageName: String,
        override val specId: BackupSpec.Id = BackupSpec.Id("pkg-$packageName"),
        override val revisionLimit: Int = 3,
        val backupApk: Boolean,
        val backupData: Boolean,
        val backupCache: Boolean,
        val extraPaths: Set<APath> = setOf(LocalPath.build("/storage/emulated/0/Download"))
) : BackupSpec {

    override fun getLabel(context: Context): String = context.packageManager.tryGetAppLabel(packageName)

    override var backupType: Backup.Type
        get() = Backup.Type.APP
        set(value) {}
}