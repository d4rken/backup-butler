package eu.darken.bb.common.file.core

import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.crumbsTo
import eu.darken.bb.common.file.core.saf.SAFPath
import eu.darken.bb.common.file.core.saf.crumbsTo

fun APath.crumbsTo(child: APath): Array<String> {
    require(this.pathType == child.pathType)

    return when (pathType) {
        APath.Type.RAW -> (this as RawPath).crumbsTo(child as RawPath)
        APath.Type.LOCAL -> (this as LocalPath).crumbsTo(child as LocalPath)
        APath.Type.SAF -> (this as SAFPath).crumbsTo(child as SAFPath)
    }
}
