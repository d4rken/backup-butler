package eu.darken.bb.backup.core.files.legacy

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore

data class LegacyFilesRestoreConfig(
        val replaceFiles: Boolean = true
) : Restore.Config {

    override val restoreType: Backup.Type = Backup.Type.FILES

}