package eu.darken.bb.storage.core.local

import eu.darken.bb.common.file.JavaPath
import eu.darken.bb.storage.core.Storage

data class LocalStorageRef(
        override val path: JavaPath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override val storageType: Storage.Type = Storage.Type.LOCAL

}