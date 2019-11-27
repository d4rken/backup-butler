package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import timber.log.Timber
import java.io.File
import java.io.InputStream

class FileRefSource(private val file: File) : MMRef.RefSource {

    private val resources = mutableSetOf<InputStream>()
    override fun open(): InputStream {
        return file.inputStream().also { resources.add(it) }
    }

    override fun release() {
        resources.toList().forEach {
            try {
                it.close()
            } catch (e: Exception) {
                Timber.tag(TAG).w("Failed to release file resource: $file <-> $it")
            }
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "FileRefSource")
    }
}