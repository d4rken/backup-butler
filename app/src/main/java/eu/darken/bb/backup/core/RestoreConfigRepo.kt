package eu.darken.bb.backup.core

import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreConfigRepo @Inject constructor(

) {
    fun getDefaultConfigs(): Single<Set<Restore.Config>> = Single.fromCallable {
        setOf(
            AppRestoreConfig(),
            FilesRestoreConfig()
        )
    }
}