package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathCached
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class LocalPathCached(
        val isDirectory: Boolean,
        override val lastModified: Date,
        override val cachedPath: LocalPath,
        override val size: Long
) : APath, APathCached {
    @IgnoredOnParcel override val path: String = cachedPath.path
    @IgnoredOnParcel override val name: String = cachedPath.name
    @IgnoredOnParcel override val pathType: APath.Type = cachedPath.pathType

}