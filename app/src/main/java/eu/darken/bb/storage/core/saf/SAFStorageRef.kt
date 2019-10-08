package eu.darken.bb.storage.core.saf

import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.storage.core.Storage

data class SAFStorageRef(
        override val path: SAFPath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override var storageType: Storage.Type
        get() = Storage.Type.SAF
        set(value) {}

}