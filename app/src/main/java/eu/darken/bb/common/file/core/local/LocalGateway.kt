package eu.darken.bb.common.file.core.local

import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.APathException
import eu.darken.bb.common.file.core.asFile
import javax.inject.Inject

@PerApp class LocalGateway @Inject constructor(

) {
    fun listFiles(path: LocalPath): List<LocalPath> {
        return path.asFile().safeListFiles().map { LocalPath.build(it) }
    }

    fun createDir(child: LocalPath): LocalPath {
        if (child.asFile().mkdirs()) {
            return child
        }
        throw APathException(child, reason = "Failed to create dir")
    }

    fun isDirectory(path: LocalPath): Boolean {
        return path.asFile().isDirectory
    }
}