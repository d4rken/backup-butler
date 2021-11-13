package eu.darken.bb.common.files.core.local

import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.error.hasCause
import eu.darken.bb.common.files.core.*
import eu.darken.bb.common.files.core.local.root.FileOpsClient
import eu.darken.bb.common.funnel.IPCFunnel
import eu.darken.bb.common.pkgs.pkgops.LibcoreTool
import eu.darken.bb.common.root.javaroot.JavaRootClient
import eu.darken.bb.common.root.javaroot.RootUnavailableException
import eu.darken.bb.common.shell.SharedShell
import eu.darken.bb.common.user.UserHandleBB
import eu.darken.rxshell.cmd.RxCmdShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import okio.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("BlockingMethodInNonBlockingContext")
@Singleton
class LocalGateway @Inject constructor(
    private val javaRootClient: JavaRootClient,
    private val deviceEnvironment: DeviceEnvironment,
    private val ipcFunnel: IPCFunnel,
    private val libcoreTool: LibcoreTool,
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : APathGateway<LocalPath, LocalPathLookup> {

    override val sharedResource = SharedResource.createKeepAlive(TAG, appScope + dispatcherProvider.IO)

    private suspend fun <T> rootOps(action: (FileOpsClient) -> T): T {
        javaRootClient.keepAliveWith(this)
        return javaRootClient.runModuleAction(FileOpsClient::class.java) {
            return@runModuleAction action(it)
        }
    }

    private val sharedUserShell = SharedShell(TAG, appScope + dispatcherProvider.IO)
    private suspend fun getShellSession(): SharedResource.Resource<RxCmdShell.Session> {
        return sharedUserShell.session.keepAliveWith(this).get()
    }

    private suspend fun <T> runIO(
        block: suspend CoroutineScope.() -> T
    ): T = withContext(dispatcherProvider.IO) { block() }

    @Throws(IOException::class)
    override suspend fun createDir(path: LocalPath): Boolean = createDir(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun createDir(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val javaChild = path.asFile()
            val canNormalWrite = javaChild.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    javaChild.mkdirs()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps { it.mkdirs(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("createDir(path=%s, mode=%s) failed.", path, mode)
            throw WriteException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun createFile(path: LocalPath): Boolean = createFile(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun createFile(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val javaChild = path.asFile()
            val canNormalWrite = javaChild.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    if (!javaChild.parentFile.exists()) javaChild.parentFile.mkdirs()
                    javaChild.createNewFile()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps { it.createNewFile(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("createFile(path=%s, mode=%s) failed.", path, mode)
            throw WriteException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun lookup(path: LocalPath): LocalPathLookup = lookup(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun lookup(path: LocalPath, mode: Mode = Mode.AUTO): LocalPathLookup = runIO {
        try {
            val javaFile = path.asFile()
            val canRead = javaFile.canRead()
            when {
                mode == Mode.NORMAL || canRead && mode == Mode.AUTO -> {
                    if (!canRead) throw ReadException(path)
                    getShellSession().use { sessionResource ->
                        path.performLookup(ipcFunnel, libcoreTool, sessionResource.item)
                    }
                }
                mode == Mode.ROOT || !canRead && mode == Mode.AUTO -> {
                    rootOps { it.lookUp(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("lookup(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    override suspend fun listFiles(path: LocalPath): List<LocalPath> = listFiles(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun listFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPath> = runIO {
        try {
            val nonRootList: List<File>? = path.asFile().listFilesSafe()
            when {
                mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> {
                    if (nonRootList == null) throw ReadException(path)
                    nonRootList.map { LocalPath.build(it) }
                }
                mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> {
                    rootOps { it.listFiles(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("listFiles(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun lookupFiles(path: LocalPath): List<LocalPathLookup> = lookupFiles(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun lookupFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPathLookup> = withContext(
        dispatcherProvider.IO
    ) {
        try {
            val nonRootList: List<LocalPath>? = path.asFile().listFilesSafe()?.map { it.toLocalPath() }

            when {
                mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> {
                    if (nonRootList == null) throw ReadException(path)
                    getShellSession().use { sessionResource ->
                        nonRootList.map { it.performLookup(ipcFunnel, libcoreTool, sessionResource.item) }
                    }
                }
                mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> {
                    rootOps { it.lookupFiles(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("lookupFiles(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun exists(path: LocalPath): Boolean = exists(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun exists(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val javaFile = path.asFile()
            val canAccessParent = javaFile.parentFile?.canReadExecute() ?: false
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canAccessParent -> {
                    javaFile.exists()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canAccessParent -> {
                    rootOps { it.exists(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("exists(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun canWrite(path: LocalPath): Boolean = canWrite(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun canWrite(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val javaFile = path.asFile()
            val canNormalWrite = javaFile.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    javaFile.canWrite()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    try {
                        rootOps { it.canWrite(path) }
                    } catch (e: Exception) {
                        if (e.hasCause(RootUnavailableException::class)) false
                        else throw e
                    }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("canWrite(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun canRead(path: LocalPath): Boolean = canRead(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun canRead(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val javaFile = path.asFile()
            val canNormalOpen = javaFile.isReadable()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalOpen -> {
                    canNormalOpen
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalOpen -> {
                    try {
                        rootOps { it.canRead(path) }
                    } catch (e: Exception) {
                        if (e.hasCause(RootUnavailableException::class)) false
                        else throw e
                    }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("canRead(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    fun isStorageRoot(path: LocalPath, userHandle: UserHandleBB): Boolean {
        return deviceEnvironment.getPublicStorage(userHandle).any { it.localPath == path }
    }

    @Throws(IOException::class)
    override suspend fun read(path: LocalPath): Source = read(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun read(path: LocalPath, mode: Mode = Mode.AUTO): Source = runIO {
        try {
            val javaFile = path.asFile()
            val canNormalOpen = javaFile.isReadable()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalOpen -> {
                    javaFile.source()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalOpen -> {
                    path.sourceRoot(javaRootClient).buffer()
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("read(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun write(path: LocalPath): Sink = write(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun write(path: LocalPath, mode: Mode = Mode.AUTO): Sink = runIO {
        try {
            val javaFile = path.asFile()
            val canOpen = javaFile.isReadable()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canOpen -> {
                    javaFile.sink()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canOpen -> {
                    path.sinkRoot(javaRootClient).buffer()
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("read(path=%s, mode=%s) failed.", path, mode)
            throw ReadException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun delete(path: LocalPath): Boolean = delete(path, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun delete(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val javaFile = path.asFile()
            val canNormalWrite = javaFile.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    if (!canNormalWrite) throw WriteException(path)
                    javaFile.delete()
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps { it.delete(path) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("delete(path=%s, mode=%s) failed.", path, mode)
            throw WriteException(path, cause = e)
        }
    }

    @Throws(IOException::class)
    override suspend fun createSymlink(linkPath: LocalPath, targetPath: LocalPath): Boolean =
        createSymlink(linkPath, targetPath, Mode.AUTO)

    @Throws(IOException::class)
    suspend fun createSymlink(linkPath: LocalPath, targetPath: LocalPath, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val linkPathJava = linkPath.asFile()
            val targetPathJava = targetPath.asFile()
            val canNormalWrite = linkPathJava.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    linkPathJava.createSymlink(targetPathJava)
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps { it.createSymlink(linkPath, targetPath) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("createSymlink(linkPath=%s, targetPath=%s, mode=%s) failed.", linkPath, targetPath, mode)
            throw WriteException(linkPath, cause = e)
        }
    }

    override suspend fun setModifiedAt(path: LocalPath, modifiedAt: Date): Boolean = setModifiedAt(
        path,
        modifiedAt,
        Mode.AUTO
    )

    suspend fun setModifiedAt(path: LocalPath, modifiedAt: Date, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val canNormalWrite = path.file.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    path.file.setLastModified(modifiedAt.time)
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps {
                        it.setModifiedAt(path, modifiedAt)
                    }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("setModifiedAt(path=%s, modifiedAt=%s, mode=%s) failed.", path, modifiedAt, mode)
            throw WriteException(path, cause = e)
        }
    }

    override suspend fun setPermissions(path: LocalPath, permissions: Permissions): Boolean =
        setPermissions(path, permissions, Mode.AUTO)

    suspend fun setPermissions(path: LocalPath, permissions: Permissions, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val canNormalWrite = path.file.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    path.file.setPermissions(permissions)
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps { it.setPermissions(path, permissions) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("setPermissions(path=%s, permissions=%s, mode=%s) failed.", path, permissions, mode)
            throw WriteException(path, cause = e)
        }
    }

    override suspend fun setOwnership(path: LocalPath, ownership: Ownership): Boolean = setOwnership(
        path,
        ownership,
        Mode.AUTO
    )

    suspend fun setOwnership(path: LocalPath, ownership: Ownership, mode: Mode = Mode.AUTO): Boolean = runIO {
        try {
            val canNormalWrite = path.file.canWrite()
            when {
                mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                    path.file.setOwnership(ownership)
                }
                mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                    rootOps { it.setOwnership(path, ownership) }
                }
                else -> throw IOException("No matching mode.")
            }
        } catch (e: IOException) {
            Timber.tag(TAG).w("setOwnership(path=%s, ownership=%s, mode=%s) failed.", path, ownership, mode)
            throw WriteException(path, cause = e)
        }
    }

    enum class Mode {
        AUTO, NORMAL, ROOT
    }

    companion object {
        val TAG = logTag("Local", "Gateway")
    }
}