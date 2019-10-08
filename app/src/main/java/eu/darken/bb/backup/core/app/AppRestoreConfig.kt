package eu.darken.bb.backup.core.app

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore

data class AppRestoreConfig(
        val skipExistingApps: Boolean = false,
        val restoreApk: Boolean = true,
        val restoreData: Boolean = true
) : Restore.Config {

    override var restoreType: Backup.Type
        get() = Backup.Type.APP
        set(value) {}

}