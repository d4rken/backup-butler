package eu.darken.bb.user.core

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class UpgradeData(
    val state: State,
    val features: Collection<Feature>,
    val validity: Long = -1L
) {

    @Keep
    enum class Feature {
        BACKUP
    }

    @Keep
    enum class State {
        BASIC, PRO
    }
}