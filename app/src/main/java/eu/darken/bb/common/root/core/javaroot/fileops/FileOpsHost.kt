package eu.darken.bb.common.root.core.javaroot.fileops

import eu.darken.bb.App
import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.file.core.local.getAPathFileType
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileOpsHost : FileOps.Stub() {
    override fun lookUp(path: RootPath): RootPathLookup = try {
        val file = path.asFile()
        RootPathLookup(
                lookedUp = path,
                size = file.length(),
                lastModifiedRaw = file.lastModified(),
                fileType = file.getAPathFileType()
        )
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookUp(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun listFiles(path: RootPath): List<RootPath> = try {
        val result = path.asFile().listFiles()
        if (result == null) throw IOException("listFiles() returned null for ${path.path}")
        result.map { it.toRootPath() }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "listFiles(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun lookupFiles(path: RootPath): List<RootPathLookup> = try {
        listFiles(path).map { lookUp(it) }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookupFiles(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun readFile(path: RootPath): RemoteInputStream = try {
        FileInputStream(path.toLocalPath().asFile()).toRemoteInputStream()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "readFile(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun writeFile(path: RootPath): RemoteOutputStream = try {
        FileOutputStream(path.toLocalPath().asFile()).toRemoteOutputStream()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "writeFile(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun mkdirs(path: RootPath): Boolean = try {
        path.asFile().mkdirs()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun createNewFile(path: RootPath): Boolean = try {
        path.asFile().createNewFile()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun canRead(path: RootPath): Boolean = try {
        path.asFile().canRead()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "path(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun canWrite(path: RootPath): Boolean = try {
        path.asFile().canWrite()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "canWrite(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun exists(path: RootPath): Boolean = try {
        path.asFile().exists()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "exists(path=$path) failed.")
        throw wrapPropagating(e)
    }

    private fun wrapPropagating(e: Exception): Exception {
        return if (e is UnsupportedOperationException) e
        else UnsupportedOperationException(e)
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "Host", "FileOps")
    }
}