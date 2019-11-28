package eu.darken.bb.common.root.core.javaroot.fileops

import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.LocalPathLookup
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import java.io.File
import java.io.InputStream
import java.io.OutputStream

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

fun RootPath.inputStream(client: JavaRootClient): InputStream {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.readFile(this).wrap { resource.close() }
}

fun RootPath.outputStream(client: JavaRootClient): OutputStream {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.writeFile(this).wrap { resource.close() }
}