package eu.darken.bb.common.root.core.javaroot.fileops

import eu.darken.bb.App
import eu.darken.bb.common.getRootCause
import timber.log.Timber
import java.io.IOException

class FileOpsClient(
        private val fileOps: FileOps
) : ClientModule {
    fun lookUp(path: RootPath): RootPathLookup = try {
        fileOps.lookUp(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookUp(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun listFiles(path: RootPath): List<RootPath> = try {
        fileOps.listFiles(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "listFiles(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun lookupFiles(path: RootPath): List<RootPathLookup> = try {
        fileOps.lookupFiles(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookupFiles(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun readFile(path: RootPath): RemoteInputStream = try {
        fileOps.readFile(path)
    } catch (e: IOException) {
        Timber.tag(TAG).e(e, "readFile(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun writeFile(path: RootPath): RemoteOutputStream = try {
        fileOps.writeFile(path)
    } catch (e: IOException) {
        Timber.tag(TAG).e(e, "writeFile(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun mkdirs(path: RootPath): Boolean = try {
        fileOps.mkdirs(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun createNewFile(path: RootPath): Boolean = try {
        fileOps.createNewFile(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun canRead(path: RootPath): Boolean = try {
        fileOps.canRead(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "path(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun canWrite(path: RootPath): Boolean = try {
        fileOps.canWrite(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "canWrite(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun exists(path: RootPath): Boolean = try {
        fileOps.exists(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "exists(path=$path) failed.")
        throw fakeIOException(e.getRootCause())
    }

    fun delete(path: RootPath): Boolean = try {
        fileOps.delete(path)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "delete(path=$path) failed.")
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