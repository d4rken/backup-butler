package eu.darken.bb.common.file.core.saf

import android.content.Context
import android.net.Uri
import androidx.annotation.Keep
import androidx.documentfile.provider.DocumentFile
import com.squareup.moshi.JsonClass
import eu.darken.bb.common.TypeMissMatchException
import eu.darken.bb.common.file.core.APath
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@Keep @Parcelize
@JsonClass(generateAdapter = true)
data class SAFPath(
        internal val treeRoot: Uri,
        internal val crumbs: List<String>
) : APath {

    init {
        require(SAFGateway.isTreeUri(treeRoot)) { "SAFFile URI's must be a tree uri: $treeRoot" }
    }

    override fun userReadableName(context: Context): String {
        // TODO
        return super.userReadableName(context)
    }

    override fun userReadablePath(context: Context): String {
        // TODO
        return super.userReadablePath(context)
    }

    override var pathType: APath.PathType
        get() = APath.PathType.SAF
        set(value) {
            TypeMissMatchException.check(value, pathType)
        }

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

    override fun child(vararg segments: String): SAFPath {
        return build(this.treeRoot, *this.crumbs.toTypedArray(), *segments)
    }

    @Throws(IOException::class)
    fun lookup(gateway: SAFGateway): SAFPathLookup = gateway.lookup(this)

    @Throws(IOException::class)
    fun listFiles(gateway: SAFGateway): List<SAFPath> = gateway.listFiles(this)

    @Throws(IOException::class)
    fun canWrite(gateway: SAFGateway) = gateway.canWrite(this)

    @Throws(IOException::class)
    fun canRead(gateway: SAFGateway) = gateway.canRead(this)

    @Throws(IOException::class)
    fun delete(gateway: SAFGateway): Boolean = gateway.delete(this)

    @Throws(IOException::class)
    fun exists(gateway: SAFGateway): Boolean = gateway.exists(this)

    @Throws(IOException::class)
    fun isFile(gateway: SAFGateway): Boolean = gateway.lookup(this).fileType == APath.FileType.FILE

    @Throws(IOException::class)
    fun isDirectory(gateway: SAFGateway): Boolean = gateway.lookup(this).fileType == APath.FileType.DIRECTORY

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
        if (isFile(gateway)) {
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
        if (isDirectory(gateway)) {
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
    if (isDirectory(gateway)) {
        listFiles(gateway).forEach { it.deleteAll(gateway) }
    }
    if (delete(gateway)) {
        Timber.v("File.release(): Deleted %s", this)
    } else if (!exists(gateway)) {
        Timber.w("File.release(): File didn't exist: %s", this)
    } else {
        throw FileNotFoundException("Failed to delete file: $this")
    }
}

fun SAFPath.walk(gateway: SAFGateway, direction: FileWalkDirection): SAFTreeWalk = SAFTreeWalk(gateway, this, direction)

fun SAFPath.walkTopDown(gateway: SAFGateway): SAFTreeWalk = walk(gateway, FileWalkDirection.TOP_DOWN)