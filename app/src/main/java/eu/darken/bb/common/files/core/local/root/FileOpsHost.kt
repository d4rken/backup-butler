package eu.darken.bb.common.files.core.local.root

import eu.darken.bb.App
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.files.core.asFile
import eu.darken.bb.common.files.core.local.*
import eu.darken.bb.common.funnel.IPCFunnel
import eu.darken.bb.common.pkgs.pkgops.LibcoreTool
import eu.darken.bb.common.shell.SharedShell
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class FileOpsHost @Inject constructor(
    private val sharedShell: SharedShell,
    private val libcoreTool: LibcoreTool,
    private val ipcFunnel: IPCFunnel
) : FileOpsConnection.Stub() {

    override fun lookUp(path: LocalPath): LocalPathLookup = try {
        sharedShell.session.get().use { sessionResource ->
            path.performLookup(ipcFunnel, libcoreTool, sessionResource.item)
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookUp(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun listFiles(path: LocalPath): List<LocalPath> = try {
        path.asFile().listFilesThrowing().map { LocalPath.build(it) }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "listFiles(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun lookupFiles(path: LocalPath): List<LocalPathLookup> = try {
        listFiles(path).map { lookUp(it) }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "lookupFiles(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun readFile(path: LocalPath): RemoteInputStream = try {
        FileInputStream(path.asFile()).remoteInputStream()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "readFile(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun writeFile(path: LocalPath): RemoteOutputStream = try {
        FileOutputStream(path.asFile()).toRemoteOutputStream()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "writeFile(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun mkdirs(path: LocalPath): Boolean = try {
        path.asFile().mkdirs()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun createNewFile(path: LocalPath): Boolean = try {
        val file = path.asFile()
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        file.createNewFile()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "mkdirs(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun canRead(path: LocalPath): Boolean = try {
        path.asFile().canRead()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "path(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun canWrite(path: LocalPath): Boolean = try {
        path.asFile().canWrite()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "canWrite(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun exists(path: LocalPath): Boolean = try {
        path.asFile().exists()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "exists(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun delete(path: LocalPath): Boolean = try {
        path.asFile().delete()
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "delete(path=$path) failed.")
        throw wrapPropagating(e)
    }

    override fun createSymlink(linkPath: LocalPath, targetPath: LocalPath): Boolean = try {
        linkPath.asFile().createSymlink(targetPath.asFile())
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "createSymlink(linkPath=$linkPath, targetPath=$targetPath) failed.")
        throw wrapPropagating(e)
    }

    override fun setModifiedAt(path: LocalPath, modifiedAt: Long): Boolean = try {
        path.asFile().setLastModified(modifiedAt)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "setModifiedAt(path=$path, modifiedAt=$modifiedAt) failed.")
        throw wrapPropagating(e)
    }

    override fun setPermissions(path: LocalPath, permissions: Permissions): Boolean = try {
        path.asFile().setPermissions(permissions)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "setModifiedAt(path=$path, permissions=$permissions) failed.")
        throw wrapPropagating(e)
    }

    override fun setOwnership(path: LocalPath, ownership: Ownership): Boolean = try {
        path.asFile().setOwnership(ownership)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "setModifiedAt(path=$path, ownership=$ownership) failed.")
        throw wrapPropagating(e)
    }

    private fun wrapPropagating(e: Exception): Exception {
        return if (e is UnsupportedOperationException) e
        else UnsupportedOperationException(e)
    }

    companion object {
        val TAG = App.logTag("Root", "Java", "FileOps", "Host")
    }
}