package eu.darken.bb.storage.core.local

import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageConfig

data class LocalStorageConfig(
        override val label: String
) : StorageConfig {
    override val storageType: BackupStorage.Type = BackupStorage.Type.LOCAL_STORAGE
}