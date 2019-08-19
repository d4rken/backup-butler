package eu.darken.bb.backup.core.file

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore

data class FileRestoreConfig(
        val replaceFiles: Boolean = true
) : Restore.Config {

    override val restoreType: Backup.Type = Backup.Type.FILE

}