package eu.darken.bb.storage.core.local

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.storage.core.Storage

@Keep
@JsonClass(generateAdapter = true)
data class LocalStorageRef(
        override val path: LocalPath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override var storageType: Storage.Type
        get() = Storage.Type.LOCAL
        set(value) {}

}