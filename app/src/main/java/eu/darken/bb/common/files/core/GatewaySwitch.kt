package eu.darken.bb.common.files.core

import eu.darken.bb.App
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.files.core.local.LocalGateway
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.core.saf.SAFPath
import okio.Sink
import okio.Source
import java.util.*
import javax.inject.Inject

@PerApp
class GatewaySwitch @Inject constructor(
        private val safGateway: SAFGateway,
        private val localGateway: LocalGateway
) : APathGateway<APath, APathLookup<APath>> {

    fun <T : APath> getGateway(path: T): APathGateway<T, APathLookup<T>> {
        @Suppress("UNCHECKED_CAST")
        val gateway = when (path.pathType) {
            APath.PathType.SAF -> safGateway
            APath.PathType.LOCAL -> localGateway
            else -> throw NotImplementedError()
        } as APathGateway<T, APathLookup<T>>
        gateway.keepAliveWith(this)
        return gateway
    }

    override val keepAlive = SharedHolder.createKeepAlive("${TAG}:SharedResource")

    override fun createDir(path: APath): Boolean {
        return getGateway(path).createDir(path)
    }

    override fun createFile(path: APath): Boolean {
        return getGateway(path).createFile(path)
    }

    override fun lookup(path: APath): APathLookup<APath> {
        return getGateway(path).lookup(path)
    }

    override fun lookupFiles(path: APath): List<APathLookup<APath>> {
        return getGateway(path).lookupFiles(path)
    }

    override fun listFiles(path: APath): List<APath> {
        return getGateway(path).listFiles(path)
    }

    override fun exists(path: APath): Boolean {
        return getGateway(path).exists(path)
    }

    override fun canWrite(path: APath): Boolean {
        return getGateway(path).canWrite(path)
    }

    override fun canRead(path: APath): Boolean {
        return getGateway(path).canRead(path)
    }

    override fun read(path: APath): Source {
        return getGateway(path).read(path)
    }

    override fun write(path: APath): Sink {
        return getGateway(path).write(path)
    }

    override fun delete(path: APath): Boolean {
        return getGateway(path).delete(path)
    }

    override fun createSymlink(linkPath: APath, targetPath: APath): Boolean {
        return getGateway(linkPath).createSymlink(linkPath, targetPath)
    }

    override fun setModifiedAt(path: APath, modifiedAt: Date): Boolean {
        return getGateway(path).setModifiedAt(path, modifiedAt)
    }

    override fun setPermissions(path: APath, permissions: Permissions): Boolean {
        return getGateway(path).setPermissions(path, permissions)
    }

    override fun setOwnership(path: APath, ownership: Ownership): Boolean {
        return getGateway(path).setOwnership(path, ownership)
    }

    fun tryReleaseResources(path: APath) {
        if (path is SAFPath) {
            safGateway.releasePermission(path)
        }
    }

    companion object {
        val TAG = App.logTag("GatewaySwitch")
    }
}