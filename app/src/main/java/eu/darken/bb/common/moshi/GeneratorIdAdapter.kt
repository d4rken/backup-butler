package eu.darken.bb.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import eu.darken.bb.backup.core.Generator
import java.util.*

class GeneratorIdAdapter {
    @ToJson
    fun toJson(item: Generator.Id): String = item.id.toString()

    @FromJson
    fun fromJson(item: String): Generator.Id = Generator.Id(UUID.fromString(item))
}
