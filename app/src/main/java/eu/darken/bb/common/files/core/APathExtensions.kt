package eu.darken.bb.common.files.core

import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.files.core.local.LocalPath
import eu.darken.bb.common.files.core.local.crumbsTo
import eu.darken.bb.common.files.core.saf.SAFPath
import eu.darken.bb.common.files.core.saf.crumbsTo
import eu.darken.bb.processor.core.mm.MMRef
import eu.darken.bb.processor.core.mm.Props
import okio.Sink
import okio.Source
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*

fun APath.crumbsTo(child: APath): Array<String> {
    require(this.pathType == child.pathType)

    return when (pathType) {
        APath.PathType.RAW -> (this as RawPath).crumbsTo(child as RawPath)
        APath.PathType.LOCAL -> (this as LocalPath).crumbsTo(child as LocalPath)
        APath.PathType.SAF -> (this as SAFPath).crumbsTo(child as SAFPath)
    }
}

@Suppress("UNCHECKED_CAST")
fun <P : APath> P.childCast(vararg segments: String): P = child(*segments) as P

fun APath.asFile(): File = when (this) {
    is LocalPath -> this.file
    else -> File(this.path)
}

fun <P : APath, PL : APathLookup<P>, GT : APathGateway<P, PL>> P.walk(gateway: GT): PathTreeFlow<P, PL, GT> {
    return PathTreeFlow(gateway, downCast())
}

fun FileType.toMMRefType(): MMRef.Type {
    return when (this) {
        FileType.DIRECTORY -> MMRef.Type.DIRECTORY
        FileType.FILE -> MMRef.Type.FILE
        FileType.SYMBOLIC_LINK -> MMRef.Type.SYMLINK
    }
}

/**
 * // FIXME not sure if this can be fixed?
 * APathLookup is a super of APath, but LocalPathLookup is not a super of LocalPath
 * The compiler allows us to pass a LocalPathLookup to a function that only takesk LocalPath
 */
internal fun <T : APath> T.downCast(): T = if (this is APathLookup<*>) {
    @Suppress("UNCHECKED_CAST")
    this.lookedUp as T
} else {
    this
}

suspend fun <T : APath> T.exists(gateway: APathGateway<T, out APathLookup<T>>): Boolean {
    return gateway.exists(downCast())
}

suspend fun <T : APath> T.requireExists(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (!exists(gateway)) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

suspend fun <T : APath> T.requireNotExists(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (exists(gateway)) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

suspend fun <T : APath> T.createFileIfNecessary(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (exists(gateway)) {
        if (gateway.lookup(downCast()).fileType == FileType.FILE) {
            Timber.v("File already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Exists, but is not a file: $this")
            Timber.w(ex)
            throw ex
        }
    }
    try {
        gateway.createFile(downCast())
        Timber.v("File created: %s", this)
        return this
    } catch (e: Exception) {
        val ex = IllegalStateException("Couldn't create file: $this", e)
        Timber.w(ex)
        throw ex
    }
}

suspend fun <T : APath> T.createDirIfNecessary(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (exists(gateway)) {
        if (gateway.lookup(downCast()).isDirectory) {
            Timber.v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Exists, but is not a directory: $this")
            Timber.w(ex)
            throw ex
        }
    }
    try {
        gateway.createDir(downCast())
        Timber.v("Directory created: %s", this)
        return this
    } catch (e: Exception) {
        val ex = IllegalStateException("Couldn't create Directory: $this", e)
        Timber.w(ex)
        throw ex
    }
}

suspend fun <T : APath> T.deleteAll(gateway: APathGateway<T, out APathLookup<T>>) {
    if (gateway.lookup(downCast()).isDirectory) {
        gateway.listFiles(downCast()).forEach { it.deleteAll(gateway) }
    }
    if (gateway.delete(this)) {
        Timber.v("File.release(): Deleted %s", this)
    } else if (!exists(gateway)) {
        Timber.w("File.release(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}

suspend fun <T : APath> T.write(gateway: APathGateway<T, out APathLookup<T>>): Sink {
    return gateway.write(downCast())
}

suspend fun <T : APath> T.read(gateway: APathGateway<T, out APathLookup<T>>): Source {
    return gateway.read(downCast())
}

suspend fun <T : APath> T.createSymlink(gateway: APathGateway<T, out APathLookup<T>>, target: T): Boolean {
    return gateway.createSymlink(downCast(), target)
}

suspend fun <T : APath> T.setMetaData(gateway: APathGateway<T, out APathLookup<T>>, props: Props): Boolean {
    val modSuc = if (props is Props.HasModifiedDate) {
        setModifiedAt(gateway, props.modifiedAt)
    } else {
        true
    }
    val permSuc = if (props is Props.HasPermissions) {
        props.permissions?.let { setPermissions(gateway, it) } ?: true
    } else {
        true
    }
    val ownSuc = if (props is Props.HasOwner) {
        props.ownership?.let { setOwnership(gateway, it) } ?: true
    } else {
        true
    }
    return modSuc && permSuc && ownSuc
}

suspend fun <T : APath> T.setModifiedAt(gateway: APathGateway<T, out APathLookup<T>>, modifiedAt: Date): Boolean {
    return gateway.setModifiedAt(downCast(), modifiedAt)
}

suspend fun <T : APath> T.setPermissions(
    gateway: APathGateway<T, out APathLookup<T>>,
    permissions: Permissions
): Boolean {
    return gateway.setPermissions(downCast(), permissions)
}

suspend fun <T : APath> T.setOwnership(gateway: APathGateway<T, out APathLookup<T>>, ownership: Ownership): Boolean {
    return gateway.setOwnership(downCast(), ownership)
}

inline fun <reified T : APath> T.relativeTo(parent: T): APath? {
    // TODO TEST
    return when (pathType) {
        APath.PathType.LOCAL -> {
            val rel = File(this.path).relativeToOrNull(File(parent.path))
            rel?.let { LocalPath.build(it.path) }
        }
//       APath.PathType.RAW -> TODO()
//       APath.PathType.SAF -> TODO()
        else -> TODO()
    }
}

suspend fun <P : APath, PLU : APathLookup<P>> P.lookup(gateway: APathGateway<P, PLU>): PLU {
    return gateway.lookup(downCast())
}

suspend fun <P : APath, PLU : APathLookup<P>> P.lookupFiles(gateway: APathGateway<P, PLU>): List<PLU> {
    return gateway.lookupFiles(downCast())
}

suspend fun <P : APath, PLU : APathLookup<P>> P.lookupFilesOrNull(gateway: APathGateway<P, PLU>): List<PLU>? {
    return if (exists(gateway)) gateway.lookupFiles(downCast()) else null
}

suspend fun <T : APath> T.listFiles(gateway: APathGateway<T, out APathLookup<T>>): List<T> {
    return gateway.listFiles(downCast())
}

suspend fun <T : APath> T.listFilesOrNull(gateway: APathGateway<T, out APathLookup<T>>): List<T>? {
    return if (exists(gateway)) gateway.listFiles(downCast()) else null
}

suspend fun <T : APath> T.canWrite(gateway: APathGateway<T, out APathLookup<T>>): Boolean {
    return gateway.canWrite(downCast())
}

suspend fun <T : APath> T.isFile(gateway: APathGateway<T, out APathLookup<T>>): Boolean {
    return gateway.lookup(downCast()).fileType == FileType.FILE
}

suspend fun <T : APath> T.isDirectory(gateway: APathGateway<T, out APathLookup<T>>): Boolean {
    return gateway.lookup(downCast()).fileType == FileType.DIRECTORY
}

suspend fun <T : APath> T.mkdirs(gateway: APathGateway<T, out APathLookup<T>>): Boolean {
    return gateway.createDir(downCast())
}

suspend fun <T : APath> T.tryMkDirs(gateway: APathGateway<T, out APathLookup<T>>): APath {
    if (exists(gateway)) {
        if (isDirectory(gateway)) {
            log(VERBOSE) { "Directory already exists, not creating: $this" }
            return this
        } else {
            throw IllegalStateException("Directory exists, but is not a directory: $this").also {
                log(VERBOSE) { "Directory exists, but is not a directory: $this:\n${it.asLog()}" }
            }
        }
    }

    if (mkdirs(gateway)) {
        log(VERBOSE) { "Directory created: $this" }
        return this
    } else {
        throw IllegalStateException("Couldn't create Directory: $this").also {
            log(VERBOSE) { "Couldn't create Directory: ${it.asLog()}" }
        }
    }
}