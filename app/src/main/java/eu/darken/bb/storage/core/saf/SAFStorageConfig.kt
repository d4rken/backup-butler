package eu.darken.bb.storage.core.saf

import eu.darken.bb.storage.core.Storage

data class SAFStorageConfig(
        override val label: String = "",
        override val storageId: Storage.Id
) : Storage.Config {
    override val storageType: Storage.Type = Storage.Type.SAF
}