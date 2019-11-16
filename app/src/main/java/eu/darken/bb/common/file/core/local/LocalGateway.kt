package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.APathException
import eu.darken.bb.common.file.core.asFile
import java.util.*
import javax.inject.Inject

@PerApp class LocalGateway @Inject constructor(

) {
    fun listFiles(path: LocalPath, mode: Mode = Mode.AUTO): List<LocalPath> {
        return path.asFile().safeListFiles().map { LocalPath.build(it) }
    }

    fun createDir(child: LocalPath, mode: Mode = Mode.AUTO): LocalPath {
        if (child.asFile().mkdirs()) {
            return child
        }
        throw APathException(child, reason = "Failed to create dir")
    }

    fun isDirectory(path: LocalPath, mode: Mode = Mode.AUTO): Boolean {
        return path.asFile().isDirectory
    }

    fun buildCachedVersion(path: LocalPath, mode: Mode = Mode.AUTO): LocalPathCached {
        val javaFile = path.asFile()
        return LocalPathCached(
                cachedPath = path,
                isDirectory = javaFile.isDirectory,
                lastModified = Date(javaFile.lastModified()),
                size = javaFile.length()
        )
    }

    enum class Mode {
        AUTO, NORMAL, ROOT
    }
}