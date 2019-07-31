package eu.darken.bb.storage.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.storage.core.local.LocalStorageRef
import java.util.*

interface StorageRef {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<StorageRef> = PolymorphicJsonAdapterFactory.of(StorageRef::class.java, "storageType")
                .withSubtype(LocalStorageRef::class.java, BackupStorage.Type.LOCAL.name)
    }

    val storageId: UUID
    val storageType: BackupStorage.Type
}
