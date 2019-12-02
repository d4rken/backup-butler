package eu.darken.bb.common.root.core.javaroot.fileops

import eu.darken.bb.App
import eu.darken.bb.common.file.core.Ownership
import eu.darken.bb.common.file.core.Permissions
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.local.LocalPathLookup
import eu.darken.bb.common.getRootCause
import okio.Sink
import okio.Source
import timber.log.Timber
import java.io.IOException
import java.util.*

class FileOpsClient(
        private val fileOps: FileOps
) : ClientModule {
    fun lookUp(path: LocalPath): LocalPathLookup = try {
        fileOps.lookUp(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookUp(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun listFiles(path: LocalPath): List<LocalPath> = try {
        fileOps.listFiles(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "listFiles(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun lookupFiles(path: LocalPath): List<LocalPathLookup> = try {
        fileOps.lookupFiles(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookupFiles(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun readFile(path: LocalPath): Source = try {
        fileOps.readFile(path).source()
    } catch (e: IOException) {
        Timber.tag(TAG).e(e, "readFile(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun writeFile(path: LocalPath): Sink = try {
        fileOps.writeFile(path).sink()
    } catch (e: IOException) {
        Timber.tag(TAG).e(e, "writeFile(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun mkdirs(path: LocalPath): Boolean = try {
        fileOps.mkdirs(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun createNewFile(path: LocalPath): Boolean = try {
        fileOps.createNewFile(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun canRead(path: LocalPath): Boolean = try {
        fileOps.canRead(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "path(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun canWrite(path: LocalPath): Boolean = try {
        fileOps.canWrite(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "canWrite(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun exists(path: LocalPath): Boolean = try {
        fileOps.exists(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "exists(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun delete(path: LocalPath): Boolean = try {
        fileOps.delete(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "delete(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun createSymlink(linkPath: LocalPath, targetPath: LocalPath): Boolean = try {
        fileOps.createSymlink(linkPath, targetPath)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "createSymlink(linkPath=$linkPath, targetPath=$targetPath) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun setModifiedAt(path: LocalPath, modifiedAt: Date): Boolean = try {
        fileOps.setModifiedAt(path, modifiedAt.time)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "setModifiedAt(path=$path, modifiedAt=$modifiedAt) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun setPermissions(path: LocalPath, permissions: Permissions): Boolean = try {
        fileOps.setPermissions(path, permissions)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "setPermissions(path=$path, permissions=$permissions) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun setOwnership(path: LocalPath, ownership: Ownership): Boolean = try {
        fileOps.setOwnership(path, ownership)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "setOwnership(path=$path, ownership=$ownership) failed.")
        throw fakeIOException(e.getRootCause())
    }

    private fun fakeIOException(e: Throwable): IOException {
        val gulpExceptionPrefix = "java.io.IOException: "
        val message = when {
            e.message.isNullOrEmpty() -> e.toString()
            e.message?.startsWith(gulpExceptionPrefix) == true -> e.message!!.replace(gulpExceptionPrefix, "")
            else -> ""
        }
        return IOException(message, e.cause)
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "Client", "FileOps")
    }
}