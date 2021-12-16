package eu.darken.bb.backup.core

import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.root.RootManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreConfigRepo @Inject constructor(
    private val rootManager: RootManager
) {
    suspend fun getDefaultConfigs(): Set<Restore.Config> = setOf(
        getAppDefaultConfig(),
        getFilesDefaultConfig(),
    )

    private suspend fun getAppDefaultConfig(): Restore.Config = AppRestoreConfig(
        skipExistingApps = true,
        restoreApk = true,
        restoreData = rootManager.isRooted(),
        restoreCache = false,
        overwriteExisting = false
    )

    private suspend fun getFilesDefaultConfig(): Restore.Config = FilesRestoreConfig()
}