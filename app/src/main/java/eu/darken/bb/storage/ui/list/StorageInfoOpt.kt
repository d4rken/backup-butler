package eu.darken.bb.storage.ui.list

import eu.darken.bb.storage.core.StorageInfo
import java.util.*

data class StorageInfoOpt(
        val storageId: UUID,
        val info: StorageInfo?
) {
    constructor(storageId: UUID) : this(storageId, null)
    constructor(config: StorageInfo) : this(config.ref.storageId, config)
}