package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathGateway
import eu.darken.bb.common.file.core.APathLookup
import okio.Source
import okio.source
import timber.log.Timber
import java.io.File

abstract class BaseRefSource(
        private val sourceFactory: () -> Source
) : MMRef.RefSource {

    private val resources = mutableSetOf<Source>()
    private var isReleased = false

    override fun open(): Source {
        require(!isReleased) { "Don't call open() on a released resource!" }
        return sourceFactory().also { resources.add(it) }
    }

    override fun release() {
        isReleased = true
        resources.toList().forEach {
            try {
                it.close()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to release file resource: $this <-> $it")
            }
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "BaseRefSource")
    }
}


class FileRefSource(private val file: File) : BaseRefSource({ file.source() })

class APathRefResource<PathType : APath, GateType : APathGateway<in PathType, out APathLookup<PathType>>>(
        private val path: PathType,
        private val gateway: GateType
) : BaseRefSource({ gateway.read(path) })