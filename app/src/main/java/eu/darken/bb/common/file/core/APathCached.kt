package eu.darken.bb.common.file.core

import androidx.annotation.Keep
import java.util.*

@Keep
interface APathCached : APath {
    val lastModified: Date
    val cachedPath: APath
    val size: Long
}