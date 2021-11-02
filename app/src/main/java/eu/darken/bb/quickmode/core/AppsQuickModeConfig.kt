package eu.darken.bb.quickmode.core

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.storage.core.Storage

@Keep
@JsonClass(generateAdapter = true)
data class AppsQuickModeConfig(
    override val storageIds: Set<Storage.Id> = emptySet()
) : QuickMode.Config {

    override val isSetUp: Boolean
        get() = storageIds.isNotEmpty()

    override var type: QuickMode.Type
        get() = QuickMode.Type.APPS
        set(_) {}
}