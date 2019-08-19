package eu.darken.bb.backup.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.file.FileRestoreConfig

interface Restore {

    interface Config {
        val restoreType: Backup.Type

        companion object {
            val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<Config> = PolymorphicJsonAdapterFactory.of(Config::class.java, "restoreType")
                    .withSubtype(AppRestoreConfig::class.java, Backup.Type.APP.name)
                    .withSubtype(FileRestoreConfig::class.java, Backup.Type.FILE.name)
        }
    }

}