package eu.darken.bb.backup.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.progress.Progress

interface Restore {
    interface Endpoint : Progress.Host {
        fun restore(config: Config, backup: Backup.Unit): Boolean
    }

    interface Config {
        val restoreType: Backup.Type

        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "restoreType")
                    .withSubtype(AppRestoreConfig::class.java, Backup.Type.APP.name)
                    .withSubtype(FilesRestoreConfig::class.java, Backup.Type.FILES.name)
        }
    }

}