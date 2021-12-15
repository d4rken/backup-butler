package eu.darken.bb.common.files.core.local.root

import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.files.core.asFile
import eu.darken.bb.common.files.core.local.*
import eu.darken.bb.common.funnel.IPCFunnel
import eu.darken.bb.common.pkgs.pkgops.LibcoreTool
import eu.darken.bb.common.shell.RootProcessShell
import eu.darken.bb.common.shell.SharedShell
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class FileOpsHost @Inject constructor(
    @RootProcessShell private val sharedShell: SharedShell,
    private val libcoreTool: LibcoreTool,
    private val ipcFunnel: IPCFunnel
) : FileOpsConnection.Stub() {

    override fun lookUp(path: LocalPath): LocalPathLookup = try {
        // TODO why use root shell here?
        runBlocking {
            sharedShell.session.get().use { sessionResource ->
                path.performLookup(ipcFunnel, libcoreTool, sessionResource.item)
            }
        }
    } catch (e: Exception) {
        log(TAG, ERROR) { "lookUp(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun listFiles(path: LocalPath): List<LocalPath> = try {
        path.asFile().listFiles2().map { LocalPath.build(it) }
    } catch (e: Exception) {
        log(TAG, ERROR) { "listFiles(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun lookupFiles(path: LocalPath): List<LocalPathLookup> = try {
        listFiles(path).map { lookUp(it) }
    } catch (e: Exception) {
        log(TAG, ERROR) { "lookupFiles(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun readFile(path: LocalPath): RemoteInputStream = try {
        FileInputStream(path.asFile()).remoteInputStream()
    } catch (e: Exception) {
        log(TAG, ERROR) { "readFile(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun writeFile(path: LocalPath): RemoteOutputStream = try {
        FileOutputStream(path.asFile()).toRemoteOutputStream()
    } catch (e: Exception) {
        log(TAG, ERROR) { "writeFile(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun mkdirs(path: LocalPath): Boolean = try {
        path.asFile().mkdirs()
    } catch (e: Exception) {
        log(TAG, ERROR) { "mkdirs(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun createNewFile(path: LocalPath): Boolean = try {
        val file = path.asFile()

        if (file.exists() && file.isDirectory) {
            throw IllegalStateException("Can't create file, path exists and is directory: $path")
        }

        file.parentFile?.let {
            if (!it.exists()) {
                if (!it.mkdirs()) {
                    log(TAG, WARN) { "Failed to create parents for $path" }
                }
            }
        }

        file.createNewFile()
    } catch (e: Exception) {
        log(TAG, ERROR) { "mkdirs(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun canRead(path: LocalPath): Boolean = try {
        path.asFile().canRead()
    } catch (e: Exception) {
        log(TAG, ERROR) { "path(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun canWrite(path: LocalPath): Boolean = try {
        path.asFile().canWrite()
    } catch (e: Exception) {
        log(TAG, ERROR) { "canWrite(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun exists(path: LocalPath): Boolean = try {
        path.asFile().exists()
    } catch (e: Exception) {
        log(TAG, ERROR) { "exists(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun delete(path: LocalPath): Boolean = try {
        path.asFile().delete()
    } catch (e: Exception) {
        log(TAG, ERROR) { "delete(path=$path) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun createSymlink(linkPath: LocalPath, targetPath: LocalPath): Boolean = try {
        linkPath.asFile().createSymlink(targetPath.asFile())
    } catch (e: Exception) {
        log(TAG, ERROR) { "createSymlink(linkPath=$linkPath, targetPath=$targetPath) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun setModifiedAt(path: LocalPath, modifiedAt: Long): Boolean = try {
        path.asFile().setLastModified(modifiedAt)
    } catch (e: Exception) {
        log(TAG, ERROR) { "setModifiedAt(path=$path, modifiedAt=$modifiedAt) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun setPermissions(path: LocalPath, permissions: Permissions): Boolean = try {
        path.asFile().setPermissions(permissions)
    } catch (e: Exception) {
        log(TAG, ERROR) { "setModifiedAt(path=$path, permissions=$permissions) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    override fun setOwnership(path: LocalPath, ownership: Ownership): Boolean = try {
        path.asFile().setOwnership(ownership)
    } catch (e: Exception) {
        log(TAG, ERROR) { "setModifiedAt(path=$path, ownership=$ownership) failed\n${e.asLog()}" }
        throw wrapPropagating(e)
    }

    private fun wrapPropagating(e: Exception): Exception {
        return if (e is UnsupportedOperationException) e
        else UnsupportedOperationException(e)
    }

    companion object {
        val TAG = logTag("Root", "Java", "FileOps", "Host")
    }
}