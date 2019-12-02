package eu.darken.bb.common.file.core.saf

import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.file.core.Ownership
import eu.darken.bb.common.file.core.Permissions
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class SAFPathLookup(
        override val lookedUp: SAFPath,
        override val size: Long,
        override val modifiedAt: Date,
        override val ownership: Ownership,
        override val permissions: Permissions,
        override val fileType: APath.FileType,
        override val target: SAFPath?
) : APathLookup<SAFPath> {

    override fun child(vararg segments: String): SAFPath {
        return lookedUp.child(*segments)
    }

    @IgnoredOnParcel override val path: String = lookedUp.path
    @IgnoredOnParcel override val name: String = lookedUp.name
    @IgnoredOnParcel override val pathType: APath.PathType = lookedUp.pathType

}