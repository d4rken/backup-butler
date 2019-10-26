package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.processor.core.mm.MMRef
import java.util.*

class MMRefIdAdapter {
    @ToJson
    fun toJson(item: MMRef.Id): String = item.value.toString()

    @FromJson
    fun fromJson(item: String): MMRef.Id = MMRef.Id(UUID.fromString(item))
}
