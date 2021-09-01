package eu.darken.bb.storage.core.saf

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.storage.core.SimpleStrategy
import eu.darken.bb.storage.core.Storage

@Keep
@JsonClass(generateAdapter = true)
data class SAFStorageConfig(
    override val label: String = "",
    override val storageId: Storage.Id,
    override val strategy: Storage.Strategy = SimpleStrategy()
) : Storage.Config {

    override var storageType: Storage.Type
        get() = Storage.Type.SAF
        set(value) {
            TypeMissMatchException.check(value, storageType)
        }

}