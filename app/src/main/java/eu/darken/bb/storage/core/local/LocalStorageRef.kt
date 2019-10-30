package eu.darken.bb.storage.core.local

import eu.darken.bb.common.file.LocalPath
import eu.darken.bb.storage.core.Storage

data class LocalStorageRef(
        override val path: LocalPath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override var storageType: Storage.Type
        get() = Storage.Type.LOCAL
        set(value) {}

}