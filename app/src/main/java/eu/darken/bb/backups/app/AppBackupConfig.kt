package eu.darken.bb.backups.app

import eu.darken.bb.backups.Backup
import eu.darken.bb.backups.BackupConfig


data class AppBackupConfig(
        val packageName: String,
        override val label: String = "pkg-$packageName"

) : BackupConfig {

    override val configType: Backup.Type = Backup.Type.APP

}