package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.APathGateway
import eu.darken.bb.common.file.core.ReadException
import eu.darken.bb.common.file.core.WriteException
import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.mapError
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.fileops.toLocalPath
import eu.darken.bb.common.root.core.javaroot.fileops.toLocalPathLookup
import eu.darken.bb.common.root.core.javaroot.fileops.toRootPath
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@PerApp class LocalGateway @Inject constructor(
        private val javaRootClient: JavaRootClient
) : APathGateway<LocalPath, LocalPathLookup> {

    val session = javaRootClient.session

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
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> javaRootClient.session
                    .map { it.ipc.fileOps }
                    .map { it.mkdirs(path.toRootPath()) }
                    .mapError { IOException(it) }
                    .blockingFirst()
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
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> javaRootClient.session
                    .map { it.ipc.fileOps }
                    .map { it.createNewFile(path.toRootPath()) }
                    .mapError { IOException(it) }
                    .blockingFirst()
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
        val javaFile = path.asFile()
        val nonRootList: Array<File>? = path.asFile().listFiles()
        when {
            mode == Mode.NORMAL || nonRootList != null && mode == Mode.AUTO -> path.asFile().listFiles().map { LocalPath.build(it) }
            mode == Mode.ROOT || nonRootList == null && mode == Mode.AUTO -> javaRootClient.session
                    .map { it.ipc.fileOps }
                    .map { it.listFiles(path.toRootPath()) }
                    .map { rps -> rps.map { it.toLocalPath() } }
                    .blockingFirst()
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
                javaRootClient.session
                        .map { it.ipc.fileOps }
                        .map { it.lookupFiles(path.toRootPath()) }
                        .map { rfs -> rfs.map { it.toLocalPathLookup() } }
                        .mapError { IOException(it) }
                        .blockingFirst()
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
            mode == Mode.ROOT || mode == Mode.AUTO && !existsNormal -> javaRootClient.session
                    .map { it.ipc.fileOps }
                    .map { it.exists(path.toRootPath()) }
                    .mapError { IOException(it) }
                    .blockingFirst()
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
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalWrite -> javaRootClient.session
                    .map { it.ipc.fileOps }
                    .map { it.canWrite(path.toRootPath()) }
                    .mapError { IOException(it) }
                    .blockingFirst()
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
            mode == Mode.ROOT || mode == Mode.AUTO && !canNormalRead -> javaRootClient.session
                    .map { it.ipc.fileOps }
                    .map { it.canRead(path.toRootPath()) }
                    .mapError { IOException(it) }
                    .blockingFirst()
            else -> throw IOException("No matching mode.")
        }
    } catch (e: IOException) {
        Timber.tag(SAFGateway.TAG).w("canRead(%s) failed.", path)
        throw ReadException(path, cause = e)
    }


    enum class Mode {
        AUTO, NORMAL, ROOT
    }
}