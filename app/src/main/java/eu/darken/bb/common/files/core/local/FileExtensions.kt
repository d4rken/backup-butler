package eu.darken.bb.common.files.core.local

import android.system.Os
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.FileType
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@Suppress("FunctionName")
fun File(vararg crumbs: String): File {
    var compacter = File(crumbs[0])
    for (i in 1 until crumbs.size) {
        compacter = File(compacter, crumbs[i])
    }
    return compacter
}

fun File.asSFile(): APath {
    return LocalPath.build(file = this)
}

fun File.requireExists(): File {
    if (!exists()) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun File.requireNotExists(): File {
    if (exists()) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun File.tryMkDirs(): File {
    if (exists()) {
        if (isDirectory) {
            Timber.v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Directory exists, but is not a directory: $this")
            Timber.w(ex)
            throw ex
        }
    }

    if (mkdirs()) {
        Timber.v("Directory created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create Directory: $this")
        Timber.w(ex)
        throw ex
    }
}

fun File.tryMkFile(): File {
    if (exists()) {
        if (isFile) {
            Timber.v("File already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Path exists but is not a file: $this")
            Timber.w(ex)
            throw ex
        }
    }

    if (!parentFile.exists()) parentFile.tryMkDirs()

    if (createNewFile()) {
        Timber.v("File created: %s", this)
        return this
    } else {
        val ex = IllegalStateException("Couldn't create file: $this")
        Timber.w(ex)
        throw ex
    }
}

@Throws(IOException::class)
fun File.deleteAll() {
    if (isDirectory) {
        listFiles()?.forEach { it.deleteAll() }
    }
    if (delete()) {
        Timber.v("File.release(): Deleted %s", this)
    } else if (!exists()) {
        Timber.w("File.release(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}

fun File.listFiles2(): List<File> {
    if (!exists()) throw IOException("File does not exist")
    if (!canRead()) throw IOException("Can't read $path")
    return this.listFiles()?.toList() ?: throw IOException("Failed to listFiles() on $path")
}

fun File.isSymbolicLink(): Boolean {
    return readLink() != null
}

fun File.createSymlink(target: File): Boolean {
    Os.symlink(target.path, this.path)
    return this.exists()
}

fun File.readLink(): String? = try {
    Os.readlink(this.path)
} catch (e: Exception) {
    null
}

fun File.isReadable(): Boolean = try {
    if (isDirectory) {
        canRead()
    } else {
        // canRead() may return true, while SELinux blocks open
        // type=1400 audit(0.0:12576): avc: denied { open } for path="/data/data/alinktests/subdir/symtarget" dev="sda45" ino=2754227 scontext=u:r:untrusted_app_27:s0:c100,c257,c512,c768 tcontext=u:object_r:system_data_file:s0 tclass=file permissive=0
        reader().use { it.read() }
        true
    }
} catch (e: Exception) {
    false
}

fun File.canReadExecute(): Boolean = canRead() && canExecute()

fun File.getAPathFileType(): FileType = when {
    // Order matters!
    isSymbolicLink() -> FileType.SYMBOLIC_LINK
    isDirectory -> FileType.DIRECTORY
    else -> FileType.FILE
}

fun File.toLocalPath(): LocalPath = LocalPath.build(this)

fun File.setPermissions(permissions: Permissions): Boolean {
    Os.chmod(path, permissions.mode)
    return true
}

fun File.setOwnership(ownership: Ownership): Boolean {
    Os.lchown(path, ownership.userId.toInt(), ownership.groupId.toInt())
    return true
}

val File.parents: Sequence<File>
    get() = sequence {
        var parent = parentFile
        while (parent != null) {
            yield(parent)
            parent = parent.parentFile
        }
    }

val File.parentsInclusive: Sequence<File>
    get() = sequenceOf(this) + parents