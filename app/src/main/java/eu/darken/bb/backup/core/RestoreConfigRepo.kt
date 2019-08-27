package eu.darken.bb.backup.core

import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.legacy.LegacyFilesRestoreConfig
import eu.darken.bb.common.dagger.PerApp
import io.reactivex.Single
import javax.inject.Inject

@PerApp
class RestoreConfigRepo @Inject constructor(

) {
    fun getDefaultConfigs(): Single<Collection<Restore.Config>> = Single.fromCallable {
        listOf(
                AppRestoreConfig(),
                LegacyFilesRestoreConfig()
        )
    }
}