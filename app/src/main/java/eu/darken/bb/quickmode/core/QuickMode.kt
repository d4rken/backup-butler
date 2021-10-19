package eu.darken.bb.quickmode.core

import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.task.core.Task

interface QuickMode {

    enum class Type {
        APPS,
        FILES
    }

    interface Config {

        val taskId: Task.Id?
        val type: Type

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> =
                MyPolymorphicJsonAdapterFactory.of(Config::class.java, "type")
                    .withSubtype(AppsQuickModeConfig::class.java, Type.APPS.name)
                    .withSubtype(FilesQuickModeConfig::class.java, Type.FILES.name)
                    .skipLabelSerialization()
        }
    }
}