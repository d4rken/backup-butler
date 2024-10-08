package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.storage.core.Storage
import java.util.*

class StorageIdAdapter {
    @ToJson
    fun toJson(item: Storage.Id): String = item.value.toString()

    @FromJson
    fun fromJson(item: String): Storage.Id = Storage.Id(UUID.fromString(item))
}
