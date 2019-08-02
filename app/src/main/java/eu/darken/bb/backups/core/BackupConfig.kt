package eu.darken.bb.backups.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backups.core.app.AppBackupConfig
import eu.darken.bb.backups.core.file.FileBackupConfig
import java.util.*

interface BackupConfig {
    val configId: UUID
    val configType: Backup.Type
    val label: String

    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<BackupConfig> = PolymorphicJsonAdapterFactory.of(BackupConfig::class.java, "configType")
                .withSubtype(AppBackupConfig::class.java, Backup.Type.APP.name)
                .withSubtype(FileBackupConfig::class.java, Backup.Type.FILE.name)
    }
}