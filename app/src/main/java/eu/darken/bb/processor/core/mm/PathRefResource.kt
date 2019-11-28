package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathGateway
import eu.darken.bb.common.file.core.APathLookup
import timber.log.Timber
import java.io.InputStream

class PathRefResource<PathType : APath, GateType : APathGateway<PathType, APathLookup<PathType>>>(
        private val path: PathType,
        private val gateway: GateType
) : MMRef.RefSource {

    private val resources = mutableSetOf<InputStream>()
    private var isReleased = false

    override fun open(): InputStream {
        require(!isReleased) { "Don't call open() on a released resource!" }
        return gateway.read(path).also { resources.add(it) }
    }

    override fun release() {
        isReleased = true
        resources.toList().forEach {
            try {
                it.close()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to release file resource: $path <-> $it")
            }
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "PathRefResource")
    }
}