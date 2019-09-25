package eu.darken.bb.common.file
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.storage.core.saf.SAFGateway
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

data class SAFPath(
        internal val treeRoot: Uri,
        internal val segments: List<String>
) : APath {

    init {
        check(SAFGateway.isTreeUri(treeRoot)) {
            "SAFFile URI's must be a tree uri: $treeRoot"
        }
    }

    override val pathType: APath.SFileType = APath.SFileType.SAF

    override val path: String
        get() = segments.joinToString(File.pathSeparator)

    override val name: String
        get() = segments.last()

    fun listFiles(gateway: SAFGateway): List<SAFPath>? = gateway.listFiles(this)

    fun canWrite(gateway: SAFGateway) = gateway.canWrite(this)

    fun child(vararg segments: String): SAFPath {
        return build(this.treeRoot, *this.segments.toTypedArray(), *segments)
    }

    fun delete(gateway: SAFGateway): Boolean = gateway.delete(this)

    fun exists(gateway: SAFGateway): Boolean = gateway.exists(this)

    override fun toString(): String = "SAFFile(treeRoot=$treeRoot, segments=$segments)"

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

fun SAFPath.isFile(gateway: SAFGateway): Boolean {
    return gateway.isFile(this)
}

fun SAFPath.isDirectory(gateway: SAFGateway): Boolean {
    return gateway.isDirectory(this)
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