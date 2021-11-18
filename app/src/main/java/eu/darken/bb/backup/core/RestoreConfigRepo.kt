package eu.darken.bb.backup.core

import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreConfigRepo @Inject constructor(

) {
    suspend fun getDefaultConfigs(): Set<Restore.Config> {
        return setOf(
            AppRestoreConfig(),
            FilesRestoreConfig()
        )
    }
}