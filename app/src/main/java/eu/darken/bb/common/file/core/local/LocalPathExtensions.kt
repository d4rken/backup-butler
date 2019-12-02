package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.file.core.callbacks
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.fileops.FileOpsClient
import okio.Sink
import okio.Source


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

fun LocalPath.sourceRoot(client: JavaRootClient): Source {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.readFile(this).callbacks { resource.close() }
}

fun LocalPath.sinkRoot(client: JavaRootClient): Sink {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.writeFile(this).callbacks { resource.close() }
}