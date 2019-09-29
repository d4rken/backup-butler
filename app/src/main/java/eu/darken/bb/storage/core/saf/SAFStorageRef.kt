package eu.darken.bb.storage.core.saf

import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.storage.core.Storage

data class SAFStorageRef(
        override val path: SAFPath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override val storageType: Storage.Type = Storage.Type.SAF

}