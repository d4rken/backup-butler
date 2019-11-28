package eu.darken.bb.common.file.core

import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.crumbsTo
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.file.core.saf.crumbsTo
import java.io.File

fun APath.crumbsTo(child: APath): Array<String> {
    require(this.pathType == child.pathType)

    return when (pathType) {
        APath.PathType.RAW -> (this as RawPath).crumbsTo(child as RawPath)
        APath.PathType.LOCAL -> (this as LocalPath).crumbsTo(child as LocalPath)
        APath.PathType.SAF -> (this as SAFPath).crumbsTo(child as SAFPath)
    }
}

fun APath.asFile(): File = when (this) {
    is LocalPath -> this.file
    else -> File(this.path)
}