package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class LocalPathLookup(
        override val lookedUp: LocalPath,
        override val size: Long,
        override val lastModified: Date,
        override val fileType: APath.FileType
) : APathLookup {
    override fun child(vararg segments: String): APath {
        return lookedUp.child(*segments)
    }

    @IgnoredOnParcel override val path: String = lookedUp.path
    @IgnoredOnParcel override val name: String = lookedUp.name
    @IgnoredOnParcel override val pathType: APath.PathType = lookedUp.pathType

}