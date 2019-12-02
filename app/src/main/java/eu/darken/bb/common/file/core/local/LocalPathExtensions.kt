package eu.darken.bb.common.file.core.local

import android.system.Os
import eu.darken.bb.common.file.core.Ownership
import eu.darken.bb.common.file.core.Permissions
import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.file.core.callbacks
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.fileops.FileOpsClient
import okio.Sink
import okio.Source
import timber.log.Timber
import java.util.*


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

internal fun LocalPath.sourceRoot(client: JavaRootClient): Source {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.readFile(this).callbacks { resource.close() }
}

internal fun LocalPath.sinkRoot(client: JavaRootClient): Sink {
    val resource = client.sharedResource.get()
    val fileOps = resource.data.getModule<FileOpsClient>()
    return fileOps.writeFile(this).callbacks { resource.close() }
}

fun LocalPath.performLookup(): LocalPathLookup {
    val fstat = try {
        Os.lstat(file.path)
    } catch (e: Exception) {
        Timber.tag(LocalGateway.TAG).w(e, "fstat failed on %s", this)
        null
    }
    return LocalPathLookup(
            fileType = file.getAPathFileType(),
            lookedUp = this,
            size = file.length(),
            modifiedAt = Date(file.lastModified()),
            ownership = Ownership(fstat?.st_uid ?: -1, fstat?.st_gid ?: -1),
            permissions = Permissions(fstat?.st_mode ?: -1),
            target = file.readLink()?.let { LocalPath.build(it) }
    )
}