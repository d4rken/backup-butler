package eu.darken.bb.storage.ui.list

import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageInfo

data class StorageInfoOpt(
        val storageId: Storage.Id,
        val info: StorageInfo?
) {
    constructor(storageId: Storage.Id) : this(storageId, null)
    constructor(config: StorageInfo) : this(config.ref.storageId, config)
}