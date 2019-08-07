package eu.darken.bb.storage.ui.list

import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageInfo

data class StorageInfoOpt(
        val storageId: BackupStorage.Id,
        val info: StorageInfo?
) {
    constructor(storageId: BackupStorage.Id) : this(storageId, null)
    constructor(config: StorageInfo) : this(config.ref.storageId, config)
}