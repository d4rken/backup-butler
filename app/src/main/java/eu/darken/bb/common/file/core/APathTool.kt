package eu.darken.bb.common.file.core

import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.file.core.local.LocalGateway
import eu.darken.bb.common.file.core.local.LocalPath
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.file.core.saf.SAFPath
import javax.inject.Inject

@PerApp
class APathTool @Inject constructor(
        private val safGateway: SAFGateway,
        private val localGateway: LocalGateway
) {

    fun getGateway(path: APath): APathGateway<APath, APathLookup<APath>> {
        return when (path) {
            is SAFPath -> safGateway as APathGateway<APath, APathLookup<APath>>
            is LocalPath -> localGateway as APathGateway<APath, APathLookup<APath>>
            else -> throw NotImplementedError()
        }
    }

    fun canWrite(path: APath): Boolean {
        return when (path) {
            is SAFPath -> path.canWrite(safGateway)
            is LocalPath -> path.canWrite(localGateway)
            else -> throw NotImplementedError()
        }
    }

    fun canRead(path: APath): Boolean {
        return when (path) {
            is SAFPath -> path.canRead(safGateway)
            is LocalPath -> path.canRead(localGateway)
            else -> throw NotImplementedError()
        }
    }

    fun tryReleaseResources(path: APath) {
        if (path is SAFPath) {
            safGateway.releasePermission(path)
        }
    }
}