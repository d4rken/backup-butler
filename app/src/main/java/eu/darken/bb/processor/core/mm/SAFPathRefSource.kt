package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import eu.darken.bb.common.file.core.saf.SAFGateway
import eu.darken.bb.common.file.core.saf.SAFPath
import timber.log.Timber
import java.io.InputStream

class SAFPathRefSource(
        private val path: SAFPath,
        private val safGateway: SAFGateway
) : MMRef.RefSource {

    private val resources = mutableSetOf<InputStream>()
    override fun open(): InputStream {
        return safGateway.read(path).also { resources.add(it) }
    }

    override fun release() {
        resources.toList().forEach {
            try {
                it.close()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to release file resource: $path <-> $it")
            }
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "SAFPathRefSource")
    }
}