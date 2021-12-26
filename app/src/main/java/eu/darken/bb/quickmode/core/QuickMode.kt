package eu.darken.bb.quickmode.core

import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.quickmode.core.apps.AppsQuickModeConfig
import eu.darken.bb.quickmode.core.files.FilesQuickModeConfig
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task

interface QuickMode {

    enum class Type {
        APPS,
        FILES
    }

    interface Config {

        val type: Type
        val isSetUp: Boolean
        val storageIds: Set<Storage.Id>
        val lastTaskId: Task.Id?

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> =
                MyPolymorphicJsonAdapterFactory.of(Config::class.java, "type")
                    .withSubtype(AppsQuickModeConfig::class.java, Type.APPS.name)
                    .withSubtype(FilesQuickModeConfig::class.java, Type.FILES.name)
                    .skipLabelSerialization()
        }
    }
}