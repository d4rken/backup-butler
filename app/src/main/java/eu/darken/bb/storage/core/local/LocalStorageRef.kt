package eu.darken.bb.storage.core.local

import eu.darken.bb.common.file.SFile
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageRef
import java.util.*

data class LocalStorageRef(
        val path: SFile,
        override val storageId: UUID = UUID.randomUUID()
) : StorageRef {

    override val storageType: BackupStorage.Type = BackupStorage.Type.LOCAL

}