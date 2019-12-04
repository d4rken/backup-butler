package eu.darken.bb.processor.core.mm

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.Ownership
import eu.darken.bb.common.file.core.Permissions
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class DirectoryProps(
        override val label: String? = null,
        override val originalPath: APath?,
        override val modifiedAt: Date,
        override val ownership: Ownership?,
        override val permissions: Permissions?
) : Props, Props.HasModifiedDate, Props.HasPermissions, Props.HasOwner {
    init {
        require(label != null || originalPath != null) { "Provide name or path!" }
    }

    @Suppress("UNUSED_PARAMETER")
    override var dataType: MMRef.Type
        get() = MMRef.Type.DIRECTORY
        set(value) {}
}