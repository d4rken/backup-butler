package eu.darken.bb.common.file

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.storage.core.saf.SAFGateway
import eu.darken.bb.storage.core.saf.SAFTreeWalk
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

data class SAFPath(
        internal val treeRoot: Uri,
        internal val crumbs: List<String>
) : APath {

    init {
        check(SAFGateway.isTreeUri(treeRoot)) {
            "SAFFile URI's must be a tree uri: $treeRoot"
        }
    }

    override val pathType: APath.SFileType = APath.SFileType.SAF

    override val path: String
        get() = if (crumbs.isNotEmpty()) {
            crumbs.joinToString(File.pathSeparator)
        } else {
            treeRoot.pathSegments.joinToString(File.pathSeparator)
        }

    override val name: String
        get() = if (crumbs.isNotEmpty()) {
            crumbs.last()
        } else {
            treeRoot.pathSegments.last().split('/').last()
        }

    fun listFiles(gateway: SAFGateway): Array<SAFPath>? = gateway.listFiles(this)

    fun canWrite(gateway: SAFGateway) = gateway.canWrite(this)

    fun child(vararg segments: String): SAFPath {
        return build(this.treeRoot, *this.crumbs.toTypedArray(), *segments)
    }

    fun delete(gateway: SAFGateway): Boolean = gateway.delete(this)

    fun exists(gateway: SAFGateway): Boolean = gateway.exists(this)

    fun isFile(gateway: SAFGateway): Boolean = gateway.isFile(this)

    fun isDirectory(gateway: SAFGateway): Boolean = gateway.isDirectory(this)

    override fun toString(): String = "SAFFile(treeRoot=$treeRoot, crumbs=$crumbs)"

    companion object {
        fun build(documentFile: DocumentFile): SAFPath {
            return build(documentFile.uri)
        }

        fun build(base: Uri, vararg segs: String): SAFPath {
            return SAFPath(base, segs.toList())
        }
    }
}

fun SAFPath.requireExists(gateway: SAFGateway): SAFPath {
    if (!this.exists(gateway)) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun SAFPath.requireNotExists(gateway: SAFGateway): SAFPath {
    if (this.exists(gateway)) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
        Timber.w(ex)
        throw ex
    }
    return this
}

fun SAFPath.tryCreateFile(gateway: SAFGateway): SAFPath {
    if (exists(gateway)) {
        if (gateway.isFile(this)) {
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

fun SAFPath.tryMkDirs(gateway: SAFGateway): SAFPath {
    if (exists(gateway)) {
        if (gateway.isDirectory(this)) {
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

fun SAFPath.deleteAll(gateway: SAFGateway) {
    if (gateway.isDirectory(this)) {
        listFiles(gateway)?.forEach { it.deleteAll(gateway) }
    }
    if (delete(gateway)) {
        Timber.v("File.deleteAll(): Deleted %s", this)
    } else if (!exists(gateway)) {
        Timber.w("File.deleteAll(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}

fun SAFPath.walk(gateway: SAFGateway, direction: FileWalkDirection): SAFTreeWalk = SAFTreeWalk(gateway, this, direction)

fun SAFPath.walkTopDown(gateway: SAFGateway): SAFTreeWalk = walk(gateway, FileWalkDirection.TOP_DOWN)

fun SAFPath.copyTo(gateway: SAFGateway, file: File): SAFPath {
    gateway.openFile(this, SAFGateway.FileMode.READ) {
        it.copyTo(file)
    }
    return this
}