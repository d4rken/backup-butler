package eu.darken.bb.backups.core.app

import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupConfig
import java.util.*


data class AppBackupConfig(
        val packageName: String? = null,
        override val configId: UUID,
        override val label: String = "pkg-$packageName"
) : BackupConfig {

    override val configType: Backup.Type = Backup.Type.APP

}