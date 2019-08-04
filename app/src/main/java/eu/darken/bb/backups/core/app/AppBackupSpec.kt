package eu.darken.bb.backups.core.app

import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.BackupSpec


data class AppBackupSpec(
        val packageName: String? = null,
        override val label: String = "pkg-$packageName"
) : BackupSpec {

    override val configType: Backup.Type = Backup.Type.APP

}