package eu.darken.bb.backups

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backups.app.AppBackupConfig
import eu.darken.bb.backups.file.FileBackupConfig

interface BackupConfig {
    val configType: Backup.Type
    val label: String

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupConfig> = PolymorphicJsonAdapterFactory.of(BackupConfig::class.java, "revisionType")
                .withSubtype(AppBackupConfig::class.java, Backup.Type.APP.name)
                .withSubtype(FileBackupConfig::class.java, Backup.Type.FILE.name)
    }
}