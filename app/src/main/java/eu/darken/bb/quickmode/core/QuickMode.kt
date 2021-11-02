package eu.darken.bb.quickmode.core

import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.storage.core.Storage

interface QuickMode {

    enum class Type {
        APPS,
        FILES
    }

    interface Config {

        val type: Type
        val isSetUp: Boolean
        val storageIds: Set<Storage.Id>

        companion object {
            val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Config> =
                MyPolymorphicJsonAdapterFactory.of(Config::class.java, "type")
                    .withSubtype(AppsQuickModeConfig::class.java, Type.APPS.name)
                    .withSubtype(FilesQuickModeConfig::class.java, Type.FILES.name)
                    .skipLabelSerialization()
        }
    }
}