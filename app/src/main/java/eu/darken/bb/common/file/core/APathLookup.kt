package eu.darken.bb.common.file.core

import androidx.annotation.Keep
import java.util.*

@Keep
interface APathLookup : APath {
    val lookedUp: APath
    val fileType: APath.FileType
    val size: Long
    val lastModified: Date

    val isDirectory: Boolean
        get() = fileType == APath.FileType.DIRECTORY

}