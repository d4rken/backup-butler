package eu.darken.bb.backup.core

import androidx.annotation.Keep
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.task.core.results.LogEvent

interface Restore {
    interface Endpoint : Progress.Host, SharedHolder.HasKeepAlive<Any> {
        fun restore(config: Config, backup: Backup.Unit, logListener: ((LogEvent) -> Unit)?)
    }

    @Keep
    interface Config {
        val restoreType: Backup.Type

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> =
                MyPolymorphicJsonAdapterFactory.of(Config::class.java, "restoreType")
                    .withSubtype(AppRestoreConfig::class.java, Backup.Type.APP.name)
                    .withSubtype(FilesRestoreConfig::class.java, Backup.Type.FILES.name)
                    .skipLabelSerialization()
        }
    }

}