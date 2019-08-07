package eu.darken.bb.storage.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.storage.core.local.LocalStorageConfig

interface StorageConfig {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<StorageConfig> = PolymorphicJsonAdapterFactory.of(StorageConfig::class.java, "storageType")
                .withSubtype(LocalStorageConfig::class.java, BackupStorage.Type.LOCAL.name)
    }

    val label: String
    val storageId: BackupStorage.Id
    val storageType: BackupStorage.Type
}