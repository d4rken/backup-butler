package eu.darken.bb.backup.core.files

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore

data class FilesRestoreConfig(
        val replaceFiles: Boolean = false
) : Restore.Config {

    override var restoreType: Backup.Type
        get() = Backup.Type.FILES
        set(value) {}

}