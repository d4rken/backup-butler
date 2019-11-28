package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.file.core.asFile


fun LocalPath.crumbsTo(child: LocalPath): Array<String> {
    val childPath = child.path
    val parentPath = this.path
    val pure = childPath.replaceFirst(parentPath, "")
    return pure.split(java.io.File.separatorChar)
            .filter { it.isNotEmpty() }
            .toTypedArray()
}

fun LocalPath.toCrumbs(): List<LocalPath> {
    val crumbs = mutableListOf<LocalPath>()
    crumbs.add(this)
    var parent = this.asFile().parentFile
    while (parent != null) {
        crumbs.add(0, LocalPath.build(parent))
        parent = parent.parentFile
    }
    return crumbs
}