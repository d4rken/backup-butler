package eu.darken.bb.processor.core.mm.generic

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.Props
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