package eu.darken.bb.processor.core.mm.archive

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.Props
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class ArchiveProps(
    override val label: String? = null,
    override val originalPath: APath?,
    override val modifiedAt: Date = Date(),
    val archiveType: String,
    val compressionType: String
) : Props, Props.HasModifiedDate {
    init {
        require(label != null || originalPath != null) { "Provide name or path!" }
    }

    @Suppress("UNUSED_PARAMETER")
    override var dataType: MMRef.Type
        get() = MMRef.Type.ARCHIVE
        set(value) {}
}