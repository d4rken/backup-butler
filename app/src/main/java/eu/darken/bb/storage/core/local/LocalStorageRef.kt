package eu.darken.bb.storage.core.local

import eu.darken.bb.common.file.SFile
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageRef

data class LocalStorageRef(
        val path: SFile,
        override val storageId: BackupStorage.Id = BackupStorage.Id()
) : StorageRef {

    override val storageType: BackupStorage.Type = BackupStorage.Type.LOCAL

}