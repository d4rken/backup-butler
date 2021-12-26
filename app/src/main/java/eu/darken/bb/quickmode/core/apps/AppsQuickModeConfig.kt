package eu.darken.bb.quickmode.core.apps

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task

@Keep
@JsonClass(generateAdapter = true)
data class AppsQuickModeConfig(
    override val storageIds: Set<Storage.Id> = emptySet(),
    override val lastTaskId: Task.Id? = null,
) : QuickMode.Config {

    override val isSetUp: Boolean
        get() = storageIds.isNotEmpty()

    override var type: QuickMode.Type
        get() = QuickMode.Type.APPS
        set(_) {}
}