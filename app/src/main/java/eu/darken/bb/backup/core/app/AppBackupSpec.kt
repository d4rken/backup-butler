package eu.darken.bb.backup.core.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec


data class AppBackupSpec(
        val packageName: String,
        override val label: String = "pkg-$packageName"
) : BackupSpec {

    override val configType: Backup.Type = Backup.Type.APP
}