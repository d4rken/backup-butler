package eu.darken.bb.common.file

import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import eu.darken.bb.storage.core.saf.SAFGateway
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

data class SAFFile(
        val mimeType: String,
        val treeRoot: Uri,
        val segments: List<String>
) : AFile {

    init {
        check(SAFGateway.isTreeUri(treeRoot)) {
            "SAFFile URI's must be a tree uri: $treeRoot"
        }
    }

    override val type: AFile.Type
        get() = when (mimeType) {
            DIR_TYPE -> AFile.Type.DIRECTORY
            else -> AFile.Type.FILE
        }

    override val pathType: AFile.SFileType = AFile.SFileType.SAF

    override val path: String
        get() = segments.joinToString(File.pathSeparator)

    override val name: String
        get() = segments.last()

    fun listFiles(gateway: SAFGateway): List<SAFFile>? = gateway.listFiles(this)

    fun canWrite(gateway: SAFGateway) = gateway.canWrite(this)

    fun child(type: AFile.Type, vararg segments: String): SAFFile {
        return build(type, this.treeRoot, *this.segments.toTypedArray(), *segments)
    }

    fun delete(gateway: SAFGateway): Boolean = gateway.delete(this)

    fun exists(gateway: SAFGateway): Boolean = gateway.exists(this)

    override fun toString(): String = "SAFFile(treeRoot=$treeRoot, segments=$segments)"

    companion object {
        fun build(documentFile: DocumentFile): SAFFile {
            val type: AFile.Type = when {
                documentFile.isFile -> AFile.Type.FILE
                documentFile.isDirectory -> AFile.Type.DIRECTORY
                else -> throw IllegalArgumentException("Neither file nor folder: $documentFile")
            }
            val mimeType = when (type) {
                AFile.Type.FILE -> documentFile.type ?: FILE_TYPE_DEFAULT
                AFile.Type.DIRECTORY -> DIR_TYPE
            }
            return build(mimeType, documentFile.uri)
        }

        fun build(type: AFile.Type, base: Uri, vararg segs: String): SAFFile {
            val mimeType = when (type) {
                AFile.Type.FILE -> FILE_TYPE_DEFAULT
                AFile.Type.DIRECTORY -> DIR_TYPE
            }
            return build(mimeType, base, *segs)
        }

        fun build(mimeType: String, base: Uri, vararg segs: String): SAFFile {
            return SAFFile(mimeType, base, segs.toList())
        }

        private const val DIR_TYPE: String = DocumentsContract.Document.MIME_TYPE_DIR
        private const val FILE_TYPE_DEFAULT: String = "application/octet-stream"
    }
}

fun SAFFile.childFile(vararg segments: String) = child(AFile.Type.FILE, *segments)

fun SAFFile.childDir(vararg segments: String) = child(AFile.Type.DIRECTORY, *segments)

fun SAFFile.requireExists(gateway: SAFGateway): SAFFile {
    if (!this.exists(gateway)) {
        val ex = IllegalStateException("Path doesn't exist, but should: $this")
        Timber.tag(TAG).w(ex)
        throw ex
    }
    return this
}

fun SAFFile.requireNotExists(gateway: SAFGateway): SAFFile {
    if (this.exists(gateway)) {
        val ex = IllegalStateException("Path exist, but shouldn't: $this")
        Timber.tag(TAG).w(ex)
        throw ex
    }
    return this
}

fun SAFFile.tryMkDirs(gateway: SAFGateway): SAFFile {
    if (exists(gateway)) {
        if (gateway.isDirectory(this)) {
            Timber.tag(TAG).v("Directory already exists, not creating: %s", this)
            return this
        } else {
            val ex = IllegalStateException("Directory exists, but is not a directory: $this")
            Timber.tag(TAG).w(ex)
            throw ex
        }
    }
    try {
        gateway.create(this)
        Timber.tag(TAG).v("Directory created: %s", this)
        return this
    } catch (e: Exception) {
        val ex = IllegalStateException("Couldn't create Directory: $this", e)
        Timber.tag(TAG).w(ex)
        throw ex
    }
}

fun SAFFile.deleteAll(gateway: SAFGateway) {
    if (gateway.isDirectory(this)) {
        listFiles(gateway)?.forEach { it.deleteAll(gateway) }
    }
    if (delete(gateway)) {
        Timber.tag(TAG).v("File.deleteAll(): Deleted %s", this)
    } else if (!exists(gateway)) {
        Timber.tag(TAG).w("File.deleteAll(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}