package eu.darken.bb.common.file.core

import androidx.annotation.Keep
import kotlinx.android.parcel.IgnoredOnParcel
import java.util.*

@Keep
interface APathLookup<T> : APath {
    val lookedUp: T
    val fileType: APath.FileType
    val size: Long
    val modifiedAt: Date
    val ownership: Ownership
    val permissions: Permissions
    val target: APath?

    @IgnoredOnParcel val isDirectory: Boolean
        get() = fileType == APath.FileType.DIRECTORY

}