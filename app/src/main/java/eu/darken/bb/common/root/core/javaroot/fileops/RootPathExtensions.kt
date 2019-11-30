package eu.darken.bb.common.root.core.javaroot.fileops

import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.LocalPathLookup
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import okio.Sink
import okio.Source
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
            lastModified = lastModified,
            target = target?.toLocalPath()
    )
}

fun RootPath.source(client: JavaRootClient): Source {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.readFile(this).callbacks { resource.close() }
}

fun RootPath.sink(client: JavaRootClient): Sink {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.writeFile(this).callbacks { resource.close() }
}