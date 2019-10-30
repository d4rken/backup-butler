package eu.darken.bb.common.file


fun LocalPath.crumbsTo(child: LocalPath): Array<String> {
    val childPath = child.path
    val parentPath = this.path
    val pure = childPath.replaceFirst(parentPath, "")
    return pure.split(java.io.File.separatorChar)
            .filter { it.isNotEmpty() }
            .toTypedArray()
}