package eu.darken.bb.backup.core

import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.common.progress.Progress

interface Restore {
    interface Endpoint : Progress.Host {
        fun restore(config: Config, backup: Backup.Unit): Boolean
    }

    interface Config {
        val restoreType: Backup.Type

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> = MyPolymorphicJsonAdapterFactory.of(Config::class.java, "restoreType")
                    .withSubtype(AppRestoreConfig::class.java, Backup.Type.APP.name)
                    .withSubtype(FilesRestoreConfig::class.java, Backup.Type.FILES.name)
                    .skipLabelSerialization()
        }
    }

}