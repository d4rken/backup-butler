package eu.darken.bb.backup.backups

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.backups.app.AppBackupConfig
import eu.darken.bb.backup.backups.file.FileBackupConfig

interface BackupConfig {
    val backupName: String
    val configType: Backup.Type

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupConfig> = PolymorphicJsonAdapterFactory.of(BackupConfig::class.java, "revisionType")
                .withSubtype(AppBackupConfig::class.java, Backup.Type.APP_BACKUP.name)
                .withSubtype(FileBackupConfig::class.java, Backup.Type.FILE.name)
    }
}