package eu.darken.bb.common.file
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.storage.core.saf.SAFGateway
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

data class SAFPath(
        internal val mimeType: String,
        internal val treeRoot: Uri,
        internal val segments: List<String>
) : APath {

    init {
        check(SAFGateway.isTreeUri(treeRoot)) {
            "SAFFile URI's must be a tree uri: $treeRoot"
        }
    }

    override val type: APath.Type
        get() = when (mimeType) {
            DIR_TYPE -> APath.Type.DIRECTORY
            else -> APath.Type.FILE
        }

    override val pathType: APath.SFileType = APath.SFileType.SAF

    override val path: String
        get() = segments.joinToString(File.pathSeparator)

    override val name: String
        get() = segments.last()

    fun listFiles(gateway: SAFGateway): List<SAFPath>? = gateway.listFiles(this)

    fun canWrite(gateway: SAFGateway) = gateway.canWrite(this)

    fun child(type: APath.Type, vararg segments: String): SAFPath {
        return build(type, this.treeRoot, *this.segments.toTypedArray(), *segments)
    }

    fun delete(gateway: SAFGateway): Boolean = gateway.delete(this)

    fun exists(gateway: SAFGateway): Boolean = gateway.exists(this)

    override fun toString(): String = "SAFFile(treeRoot=$treeRoot, segments=$segments)"

    companion object {
        fun build(documentFile: DocumentFile): SAFPath {
            val type: APath.Type = when {
                documentFile.isFile -> APath.Type.FILE
                documentFile.isDirectory -> APath.Type.DIRECTORY
                else -> throw IllegalArgumentException("Neither file nor folder: $documentFile")
            }
            val mimeType = when (type) {
                APath.Type.FILE -> documentFile.type ?: FILE_TYPE_DEFAULT
                APath.Type.DIRECTORY -> DIR_TYPE
            }
            return build(mimeType, documentFile.uri)
        }

        fun build(type: APath.Type, base: Uri, vararg segs: String): SAFPath {
            val mimeType = when (type) {
                APath.Type.FILE -> FILE_TYPE_DEFAULT
                APath.Type.DIRECTORY -> DIR_TYPE
            }
            return build(mimeType, base, *segs)
        }

        fun build(mimeType: String, base: Uri, vararg segs: String): SAFPath {
            return SAFPath(mimeType, base, segs.toList())
        }

        private const val DIR_TYPE: String = DocumentsContract.Document.MIME_TYPE_DIR
        private const val FILE_TYPE_DEFAULT: String = "application/octet-stream"
    }
}


fun SAFPath.childFile(vararg segments: String) = child(APath.Type.FILE, *segments)

fun SAFPath.childDir(vararg segments: String) = child(APath.Type.DIRECTORY, *segments)

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

fun SAFPath.tryMkDirs(gateway: SAFGateway): SAFPath {
    if (exists(gateway)) {
        if (gateway.isDirectory(this)) {
            Timber.v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Directory exists, but is not a directory: $this")
            Timber.w(ex)
            throw ex
        }
    }
    try {
        gateway.create(this)
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