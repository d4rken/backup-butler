package eu.darken.bb.storage.core.local

import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageConfig
import java.util.*

data class LocalStorageConfig(
        override val label: String,
        override val storageId: UUID
) : StorageConfig {
    override val storageType: BackupStorage.Type = BackupStorage.Type.LOCAL
}