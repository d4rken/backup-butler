package eu.darken.bb.common.file.core.local

import eu.darken.bb.App
import eu.darken.bb.common.SharedResource
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.*
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.fileops.*
import okio.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@PerApp
class LocalGateway @Inject constructor(
        private val javaRootClient: JavaRootClient,
        private val devEnvironment: DevEnvironment
) : APathGateway<LocalPath, LocalPathLookup> {

    override val resourceTokens = SharedResource<LocalGateway>("$TAG:SharedResource") { emitter ->
        try {
            emitter.onAvailable(this@LocalGateway)
        } catch (e: Throwable) {
            emitter.onError(e)
        }
    }

    private fun <T> rootOps(action: (FileOpsClient) -> T): T {
        if (!javaRootClient.sharedResource.isOpen) {
            try {
                val rootResource = javaRootClient.sharedResource.get()
                resourceTokens.addChildResource(rootResource)
            } catch (e: IOException) {
                Timber.tag(TAG).d("Couldn't open root client: %s", e.message)
            }
        }

        return javaRootClient.runModuleAction(FileOpsClient::class.java) {
            return@runModuleAction action(it)
        }
    }

    @Throws(IOException::class)
    override fun createDir(path: LocalPath): Boolean = createDir(path, Mode.AUTO)

    @Throws(IOException::class)
    fun createDir(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaChild = path.asFile()
        val canNormalWrite = javaChild.canWrite()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                javaChild.mkdirs()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                rootOps {
                    it.mkdirs(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("createDir(path=%s, mode=%s) failed.", path, mode)
        throw WriteException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun createFile(path: LocalPath): Boolean = createFile(path, Mode.AUTO)

    @Throws(IOException::class)
    fun createFile(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaChild = path.asFile()
        val canNormalWrite = javaChild.canWrite()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                javaChild.createNewFile()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                rootOps {
                    it.createNewFile(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("createFile(path=%s, mode=%s) failed.", path, mode)
        throw WriteException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun lookup(path: LocalPath): LocalPathLookup = lookup(path, Mode.AUTO)

    @Throws(IOException::class)
    fun lookup(path: LocalPath, mode: Mode = Mode.AUTO): LocalPathLookup = try {
        val javaFile = path.asFile()
        val canRead = javaFile.canRead()
        when {
            mode == Mode.NORMAL || canRead && mode == Mode.AUTO -> {
                if (!canRead) throw ReadException(path)
                javaFile.toLocalPathLookup()
            }
            mode == Mode.ROOT || !canRead && mode == Mode.AUTO -> {
                rootOps { it.lookUp(path.toRootPath()) }.toLocalPathLookup()
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("lookup(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    override fun listFiles(path: LocalPath): List<LocalPath> = listFiles(path, Mode.AUTO)

    @Throws(IOException::class)
    fun listFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPath> = try {
        val nonRootList: Array<File>? = path.asFile().listFiles()
        when {
            mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> {
                path.asFile().listFiles().map { LocalPath.build(it) }
            }
            mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> {
                rootOps {
                    it.listFiles(path.toRootPath())
                }.map { it.toLocalPath() }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("listFiles(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun lookupFiles(path: LocalPath): List<LocalPathLookup> = lookupFiles(path, Mode.AUTO)

    @Throws(IOException::class)
    fun lookupFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPathLookup> = try {
        val nonRootList: Array<File>? = path.asFile().listFiles()
        when {
            mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> {
                if (nonRootList == null) throw ReadException(path)
                nonRootList.map { it.toLocalPathLookup() }
            }
            mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> {
                rootOps {
                    it.lookupFiles(path.toRootPath())
                }.map { it.toLocalPathLookup() }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("lookupFiles(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun exists(path: LocalPath): Boolean = exists(path, Mode.AUTO)

    @Throws(IOException::class)
    fun exists(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val existsNormal = javaFile.exists()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && existsNormal -> {
                javaFile.exists()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !existsNormal -> {
                rootOps {
                    it.exists(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("exists(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun canWrite(path: LocalPath): Boolean = canWrite(path, Mode.AUTO)

    @Throws(IOException::class)
    fun canWrite(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val canNormalWrite = javaFile.canWrite()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                javaFile.canWrite()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                rootOps {
                    it.canWrite(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("canWrite(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun canRead(path: LocalPath): Boolean = canRead(path, Mode.AUTO)

    @Throws(IOException::class)
    fun canRead(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val canNormalOpen = javaFile.canOpenRead()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalOpen -> {
                canNormalOpen
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalOpen -> {
                rootOps {
                    it.canRead(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("canRead(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    fun isStorageRoot(path: LocalPath): Boolean {
        // TODO what about secondary storages?
        return devEnvironment.publicDeviceStorages.map { it.path }.contains(path)
    }

    @Throws(IOException::class)
    override fun read(path: LocalPath): Source = read(path, Mode.AUTO)

    @Throws(IOException::class)
    fun read(path: LocalPath, mode: Mode = Mode.AUTO): Source = try {
        val javaFile = path.asFile()
        val canNormalOpen = javaFile.canOpenRead()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalOpen -> {
                javaFile.source()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalOpen -> {
                path.toRootPath().source(javaRootClient).buffer()
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("read(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun write(path: LocalPath): Sink = write(path, Mode.AUTO)

    @Throws(IOException::class)
    fun write(path: LocalPath, mode: Mode = Mode.AUTO): Sink = try {
        val javaFile = path.asFile()
        val canOpen = javaFile.canOpenRead()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canOpen -> {
                javaFile.sink()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canOpen -> {
                path.toRootPath().sink(javaRootClient).buffer()
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("read(path=%s, mode=%s) failed.", path, mode)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun delete(path: LocalPath): Boolean = delete(path, Mode.AUTO)

    @Throws(IOException::class)
    fun delete(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val canNormalWrite = javaFile.canWrite()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                if (!canNormalWrite) throw WriteException(path)
                javaFile.delete()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                rootOps { it.delete(path.toRootPath()) }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("delete(path=%s, mode=%s) failed.", path, mode)
        throw WriteException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun createSymlink(linkPath: LocalPath, targetPath: LocalPath): Boolean = createSymlink(linkPath, targetPath, Mode.AUTO)

    @Throws(IOException::class)
    fun createSymlink(linkPath: LocalPath, targetPath: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val linkPathJava = linkPath.asFile()
        val targetPathJava = targetPath.asFile()
        val canNormalWrite = linkPathJava.canWrite()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> {
                linkPathJava.createSymlink(targetPathJava)
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                rootOps {
                    it.createSymlink(linkPath.toRootPath(), targetPath.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(TAG).w("createSymlink(linkPath=%s, targetPath=%s, mode=%s) failed.", linkPath, targetPath, mode)
        throw WriteException(linkPath, cause = e)
    }

    private fun File.toLocalPathLookup(): LocalPathLookup = LocalPathLookup(
            lookedUp = LocalPath.build(this),
            fileType = this.getAPathFileType(),
            lastModified = Date(this.lastModified()),
            size = this.length(),
            target = this.readLink()?.let { LocalPath.build(it) }
    )


    enum class Mode {
        AUTO, NORMAL, ROOT
    }

    companion object {
        val TAG = App.logTag("Local", "Gateway")
    }
}