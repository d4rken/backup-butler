package eu.darken.bb.backups.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backups.core.app.AppBackupSpec
import eu.darken.bb.backups.core.file.FileBackupSpec

interface BackupSpec {
    val configType: Backup.Type
    val label: String

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupSpec> = PolymorphicJsonAdapterFactory.of(BackupSpec::class.java, "configType")
                .withSubtype(AppBackupSpec::class.java, Backup.Type.APP.name)
                .withSubtype(FileBackupSpec::class.java, Backup.Type.FILE.name)
    }

}