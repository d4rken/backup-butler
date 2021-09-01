package eu.darken.bb.storage.core

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class SimpleStrategy(
    override val type: Storage.Strategy.Type = Storage.Strategy.Type.SIMPLE
) : Storage.Strategy {

    init {
        require(type == Storage.Strategy.Type.SIMPLE)
    }

}