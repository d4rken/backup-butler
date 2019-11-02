package eu.darken.bb.storage.core.saf

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.file.SAFPath
import eu.darken.bb.storage.core.Storage

@Keep
@JsonClass(generateAdapter = true)
data class SAFStorageRef(
        override val path: SAFPath,
        override val storageId: Storage.Id = Storage.Id()
) : Storage.Ref {

    override var storageType: Storage.Type
        get() = Storage.Type.SAF
        set(value) {}

}