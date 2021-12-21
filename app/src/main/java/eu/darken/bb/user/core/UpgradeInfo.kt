package eu.darken.bb.user.core

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.time.Instant

@Keep
@JsonClass(generateAdapter = true)
data class UpgradeInfo(
    val status: Status,
    val updatedAt: Instant = Instant.now()
) {

    @Keep
    enum class Status {
        BASIC,
        PRO
    }
}