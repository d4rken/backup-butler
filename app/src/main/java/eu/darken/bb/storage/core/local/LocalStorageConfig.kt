package eu.darken.bb.storage.core.local

import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.storage.core.SimpleStrategy
import eu.darken.bb.storage.core.Storage

data class LocalStorageConfig(
        override val storageId: Storage.Id,
        override val label: String = "",
        override val strategy: Storage.Strategy = SimpleStrategy()
) : Storage.Config {

    override var storageType: Storage.Type
        get() = Storage.Type.LOCAL
        set(value) {
            TypeMissMatchException.check(value, storageType)
        }
}