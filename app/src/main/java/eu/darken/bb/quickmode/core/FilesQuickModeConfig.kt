package eu.darken.bb.quickmode.core

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.task.core.Task

@Keep
@JsonClass(generateAdapter = true)
data class FilesQuickModeConfig(
    override val taskId: Task.Id? = null
) : QuickMode.Config {

    override var type: QuickMode.Type
        get() = QuickMode.Type.FILES
        set(_) {}
}