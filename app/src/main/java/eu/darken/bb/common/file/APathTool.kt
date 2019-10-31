package eu.darken.bb.common.file

import eu.darken.bb.common.dagger.PerApp
import javax.inject.Inject

@PerApp
class APathTool @Inject constructor(
        private val safGateway: SAFGateway
) {

    fun canWrite(path: APath): Boolean {
        // TODO support root
        return when (path) {
            is SAFPath -> path.canWrite(safGateway)
            is LocalPath -> path.file.canWrite()
            else -> throw NotImplementedError()
        }
    }

    fun canRead(path: APath): Boolean {
        // TODO support root
        return when (path) {
            is SAFPath -> path.canRead(safGateway)
            is LocalPath -> path.file.canRead()
            else -> throw NotImplementedError()
        }
    }
}