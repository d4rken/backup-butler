package eu.darken.bb.storage.core.local

import eu.darken.bb.common.file.APath
import eu.darken.bb.storage.core.Storage

data class LocalStorageRef(
        val path: APath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override val storageType: Storage.Type = Storage.Type.LOCAL

}