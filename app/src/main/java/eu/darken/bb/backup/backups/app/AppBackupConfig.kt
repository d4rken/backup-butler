package eu.darken.bb.backup.backups.app

import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.BackupConfig


data class AppBackupConfig(
        val packageName: String,
        override val backupName: String = "pkg-$packageName"

) : BackupConfig {

    override val configType: Backup.Type = Backup.Type.APP_BACKUP

}