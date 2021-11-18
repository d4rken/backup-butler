package eu.darken.bb.common.files.core

import androidx.annotation.Keep
import kotlinx.parcelize.IgnoredOnParcel
import java.util.*

@Keep
interface APathLookup<out T> : APath {
    val lookedUp: T
    val fileType: FileType
    val size: Long
    val modifiedAt: Date
    val ownership: Ownership?
    val permissions: Permissions?
    val target: APath?

    @IgnoredOnParcel val isDirectory: Boolean
        get() = fileType == FileType.DIRECTORY

    @IgnoredOnParcel val isSymlink: Boolean
        get() = fileType == FileType.SYMBOLIC_LINK

    @IgnoredOnParcel val isFile: Boolean
        get() = fileType == FileType.FILE
}