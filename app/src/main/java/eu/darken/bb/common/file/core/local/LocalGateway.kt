package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.*
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.fileops.FileOpsClient
import eu.darken.bb.common.root.core.javaroot.fileops.toLocalPath
import eu.darken.bb.common.root.core.javaroot.fileops.toLocalPathLookup
import eu.darken.bb.common.root.core.javaroot.fileops.toRootPath
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@PerApp class LocalGateway @Inject constructor(
        private val javaRootClient: JavaRootClient,
        private val devEnvironment: DevEnvironment
) : APathGateway<LocalPath, LocalPathLookup> {

    private fun <T> runRootFileOps(action: (FileOpsClient) -> T): T {
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
                runRootFileOps {
                    it.mkdirs(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("createDir(%s) failed.", path)
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
                runRootFileOps {
                    it.createNewFile(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("createFile(%s) failed.", path)
        throw WriteException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun lookup(path: LocalPath): LocalPathLookup = lookup(path, Mode.AUTO)

    @Throws(IOException::class)
    fun lookup(path: LocalPath, mode: Mode = Mode.AUTO): LocalPathLookup = try {
        // TODO root lookup?
        // TODO what if file doesn't exist, exception?
        val javaFile = path.asFile()
        LocalPathLookup(
                lookedUp = path,
                fileType = javaFile.getAPathFileType(),
                lastModified = Date(javaFile.lastModified()),
                size = javaFile.length()
        )
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("lookup(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    override fun listFiles(path: LocalPath): List<LocalPath> = listFiles(path, Mode.AUTO)

    @Throws(IOException::class)
    fun listFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPath> = try {
        // TODO what if file doesn't exist, exception?
        val nonRootList: Array<File>? = path.asFile().listFiles()
        when {
            mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> path.asFile().listFiles().map { LocalPath.build(it) }
            mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> {
                runRootFileOps {
                    it.listFiles(path.toRootPath())
                }.map { it.toLocalPath() }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("listFiles(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun lookupFiles(path: LocalPath): List<LocalPathLookup> = lookupFiles(path, Mode.AUTO)

    @Throws(IOException::class)
    fun lookupFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPathLookup> = try {
        // TODO what if file doesn't exist, exception?
        val nonRootList: Array<File>? = path.asFile().listFiles()
        when {
            mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> {
                if (nonRootList == null) throw ReadException(path)
                nonRootList.map {
                    LocalPathLookup(
                            lookedUp = LocalPath.build(it),
                            fileType = it.getAPathFileType(),
                            lastModified = Date(it.lastModified()),
                            size = it.length()
                    )
                }
            }
            mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> {
                runRootFileOps {
                    it.lookupFiles(path.toRootPath())
                }.map { it.toLocalPathLookup() }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("lookupFiles(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun exists(path: LocalPath): Boolean = exists(path, Mode.AUTO)

    @Throws(IOException::class)
    fun exists(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val existsNormal = javaFile.exists()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && existsNormal -> javaFile.exists()
            mode == Mode.ROOT || mode == Mode.AUTO && !existsNormal -> {
                runRootFileOps {
                    it.exists(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("exists(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun canWrite(path: LocalPath): Boolean = canWrite(path, Mode.AUTO)

    @Throws(IOException::class)
    fun canWrite(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val canNormalWrite = javaFile.canWrite()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalWrite -> javaFile.canWrite()
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> {
                runRootFileOps {
                    it.canWrite(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("canWrite(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    @Throws(IOException::class)
    override fun canRead(path: LocalPath): Boolean = canRead(path, Mode.AUTO)

    @Throws(IOException::class)
    fun canRead(path: LocalPath, mode: Mode = Mode.AUTO): Boolean = try {
        val javaFile = path.asFile()
        val canNormalRead = javaFile.canRead()
        when {
            mode == Mode.NORMAL || mode == Mode.AUTO && canNormalRead -> javaFile.canRead()
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalRead -> {
                runRootFileOps {
                    it.canRead(path.toRootPath())
                }
            }
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("canRead(%s) failed.", path)
        throw ReadException(path, cause = e)
    }

    fun isStorageRoot(path: LocalPath): Boolean {
        // TODO what about secondary storages?
        return devEnvironment.publicDeviceStorages.map { it.path }.contains(path)
    }

    fun delete(path: LocalPath, mode: Mode = Mode.AUTO): Boolean {
        TODO()
    }

    enum class Mode {
        AUTO, NORMAL, ROOT
    }
}