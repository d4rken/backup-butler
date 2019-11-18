package eu.darken.bb.common.root.core.javaroot.fileops

import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.LocalPathLookup
import java.io.File

fun RootPath.toLocalPath(): LocalPath {
    return LocalPath.build(path)
}

fun LocalPath.toRootPath(): RootPath {
    return RootPath.build(path)
}

fun File.toRootPath(): RootPath {
    return RootPath.build(this)
}

fun RootPathLookup.toLocalPathLookup(): LocalPathLookup {
    return LocalPathLookup(
            lookedUp = lookedUp.toLocalPath(),
            fileType = fileType,
            size = size,
            lastModified = lastModified
    )
}