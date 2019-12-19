package eu.darken.bb.common.files.core

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

fun APath.asFile(): File = when (this) {
    is LocalPath -> this.file
    else -> File(this.path)
}

fun <PT : APath, GT : APathGateway<PT, out APathLookup<PT>>> PT.walk(
        gateway: GT,
        direction: FileWalkDirection = FileWalkDirection.TOP_DOWN
): APathTreeWalk<PT, GT> {
    return APathTreeWalk(gateway, this, direction)
}

fun APath.FileType.toMMRefType(): MMRef.Type {
    return when (this) {
        APath.FileType.DIRECTORY -> MMRef.Type.DIRECTORY
        APath.FileType.FILE -> MMRef.Type.FILE
        APath.FileType.SYMBOLIC_LINK -> MMRef.Type.SYMLINK
    }
}

fun <T : APath> T.exists(gateway: APathGateway<T, out APathLookup<T>>): Boolean {
    return gateway.exists(this)
}


fun <T : APath> T.requireExists(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (!exists(gateway)) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun <T : APath> T.requireNotExists(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (exists(gateway)) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun <T : APath> T.createFileIfNecessary(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (exists(gateway)) {
        if (gateway.lookup(this).fileType == APath.FileType.FILE) {
            Timber.v("File already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Exists, but is not a file: $this")
            Timber.w(ex)
            throw ex
        }
    }
    try {
        gateway.createFile(this)
        Timber.v("File created: %s", this)
        return this
    } catch (e: Exception) {
        val ex = IllegalStateException("Couldn't create file: $this", e)
        Timber.w(ex)
        throw ex
    }
}

fun <T : APath> T.createDirIfNecessary(gateway: APathGateway<T, out APathLookup<T>>): T {
    if (exists(gateway)) {
        if (gateway.lookup(this).isDirectory) {
            Timber.v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Exists, but is not a directory: $this")
            Timber.w(ex)
            throw ex
        }
    }
    try {
        gateway.createDir(this)
        Timber.v("Directory created: %s", this)
        return this
    } catch (e: Exception) {
        val ex = IllegalStateException("Couldn't create Directory: $this", e)
        Timber.w(ex)
        throw ex
    }
}

fun <T : APath> T.deleteAll(gateway: APathGateway<T, out APathLookup<T>>) {
    if (gateway.lookup(this).isDirectory) {
        gateway.listFiles(this).forEach { it.deleteAll(gateway) }
    }
    if (gateway.delete(this)) {
        Timber.v("File.release(): Deleted %s", this)
    } else if (!exists(gateway)) {
        Timber.w("File.release(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}

fun <T : APath> T.write(gateway: APathGateway<T, out APathLookup<T>>): Sink {
    return gateway.write(this)
}

fun <T : APath> T.read(gateway: APathGateway<T, out APathLookup<T>>): Source {
    return gateway.read(this)
}

fun <T : APath> T.createSymlink(gateway: APathGateway<T, out APathLookup<T>>, target: T): Boolean {
    return gateway.createSymlink(this, target)
}

fun <T : APath> T.setMetaData(gateway: APathGateway<T, out APathLookup<T>>, props: Props): Boolean {
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
    val allSuc = modSuc && permSuc && ownSuc
    if (!allSuc) {
        Timber.w(
                "setMetaData(props=%s): setModifiedAt()=%b, setPermissions()=%b, setOwnerShip()=%b",
                props, modSuc, permSuc, ownSuc
        )
    }
    return allSuc
}

fun <T : APath> T.lookup(gateway: APathGateway<T, out APathLookup<T>>): APathLookup<T> {
    return gateway.lookup(this)
}

fun <T : APath> T.setModifiedAt(gateway: APathGateway<T, out APathLookup<T>>, modifiedAt: Date): Boolean {
    return gateway.setModifiedAt(this, modifiedAt)
}

fun <T : APath> T.setPermissions(gateway: APathGateway<T, out APathLookup<T>>, permissions: Permissions): Boolean {
    return gateway.setPermissions(this, permissions)
}

fun <T : APath> T.setOwnership(gateway: APathGateway<T, out APathLookup<T>>, ownership: Ownership): Boolean {
    return gateway.setOwnership(this, ownership)
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