package eu.darken.bb.processor.core.mm

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.Ownership
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class SymlinkProps(
        override val label: String? = null,
        override val originalPath: APath?,
        override val modifiedAt: Date,
        override val ownership: Ownership?,
        val symlinkTarget: APath
) : Props, Props.HasModifiedDate, Props.HasOwner {
    init {
        require(label != null || originalPath != null) { "Provide name or path!" }
    }

    @Suppress("UNUSED_PARAMETER")
    override var dataType: MMRef.Type
        get() = MMRef.Type.SYMLINK
        set(value) {}
}