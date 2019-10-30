package eu.darken.bb.common.file

fun APath.crumbsTo(child: APath): Array<String> {
    require(this.pathType == child.pathType)

    return when (pathType) {
        APath.Type.RAW -> (this as RawPath).crumbsTo(child as RawPath)
        APath.Type.LOCAL -> (this as LocalPath).crumbsTo(child as LocalPath)
        APath.Type.SAF -> (this as SAFPath).crumbsTo(child as SAFPath)
    }
}
