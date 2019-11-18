package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.ReadException
import eu.darken.bb.common.file.core.WriteException
import eu.darken.bb.common.file.core.asFile
import eu.darken.bb.common.mapError
import eu.darken.bb.common.root.core.javaroot.JavaRootClient
import eu.darken.bb.common.root.core.javaroot.fileops.toLocalPathLookup
import eu.darken.bb.common.root.core.javaroot.fileops.toRootPath
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

@PerApp class LocalGateway @Inject constructor(
        private val javaRootClient: JavaRootClient
) {
//    fun listFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPath> {
//        val javaFile = path.asFile()
//        if (javaFile.canRead()) {
//            return path.asFile().safeListFiles().map { LocalPath.build(it) }
//        } else {
//            return javaRootClient.session
//                    .map { it.ipc.fileOps }
//                    .map { it.listFiles(path.toRootPath()) }
//                    .map { rps -> rps.map { it.toLocalPath() } }
//                    .blockingFirst()
//        }
//    }

    val session = javaRootClient.session

    fun createDir(child: LocalPath, mode: Mode = Mode.AUTO): Boolean {
        val javaChild = child.asFile()
        return when {
            mode == Mode.NORMAL || mode == Mode.AUTO && javaChild.canWrite() -> {
                javaChild.mkdirs()
            }
            mode == Mode.ROOT || mode == Mode.AUTO && !javaChild.canWrite() -> {
                val fileOps = session.map { it.ipc.fileOps }.blockingFirst()
                fileOps.mkdirs(child.toRootPath())
            }
            else -> throw WriteException(child, "Failed to create dir.")
        }
    }

    fun isDirectory(path: LocalPath, mode: Mode = Mode.AUTO): Boolean {
        return path.asFile().isDirectory
    }

    fun lookup(path: LocalPath, mode: Mode = Mode.AUTO): LocalPathLookup {
        val javaFile = path.asFile()

        return LocalPathLookup(
                lookedUp = path,
                fileType = javaFile.getAPathFileType(),
                lastModified = Date(javaFile.lastModified()),
                size = javaFile.length()
        )
    }

    @Throws(IOException::class)
    fun lookupFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPathLookup> {
        val nonRootList: Array<File>? = path.asFile().listFiles()
        return when {
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
            else -> throw ReadException(path)
        }
    }

    enum class Mode {
        AUTO, NORMAL, ROOT
    }
}