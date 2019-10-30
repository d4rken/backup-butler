package eu.darken.bb.common.file

import eu.darken.bb.common.dagger.PerApp
import javax.inject.Inject

@PerApp
class APathTool @Inject constructor(
        private val safGateway: SAFGateway
) {

    fun canWrite(path: APath): Boolean {
        // TODO detect whether we can write to this path
        return when (path) {
            is SAFPath -> path.canWrite(safGateway)
            is LocalPath -> path.file.canWrite()
            else -> throw NotImplementedError()
        }
    }
}