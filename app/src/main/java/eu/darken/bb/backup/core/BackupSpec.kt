package eu.darken.bb.backup.core

import android.content.Context
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.app.AppBackupSpec
import eu.darken.bb.backup.core.file.FileBackupSpec

interface BackupSpec {
    val backupType: Backup.Type
    val identifier: String

    fun getLabel(context: Context): String

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupSpec> = PolymorphicJsonAdapterFactory.of(BackupSpec::class.java, "backupType")
                .withSubtype(AppBackupSpec::class.java, Backup.Type.APP.name)
                .withSubtype(FileBackupSpec::class.java, Backup.Type.FILE.name)
    }

}