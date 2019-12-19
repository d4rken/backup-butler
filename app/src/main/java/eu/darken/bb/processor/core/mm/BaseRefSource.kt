package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.APathLookup
import eu.darken.bb.common.files.core.toMMRefType
import eu.darken.bb.processor.core.mm.file.FileProps
import okio.Source
import timber.log.Timber
import java.io.Closeable

abstract class BaseRefSource : MMRef.RefSource {

    internal val resources = mutableSetOf<Closeable>()
    internal var isReleased = false

    override fun open(): Source {
        require(!isReleased) { "Don't call open() on a released resource!" }
        return doOpen().also { resources.add(it) }
    }

    abstract fun doOpen(): Source

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

    fun APathLookup<APath>.toProps(): Props {
        return when (fileType.toMMRefType()) {
            MMRef.Type.FILE -> FileProps(
                    originalPath = lookedUp,
                    modifiedAt = modifiedAt,
                    ownership = ownership,
                    permissions = permissions
            )
            MMRef.Type.DIRECTORY -> DirectoryProps(
                    originalPath = lookedUp,
                    modifiedAt = modifiedAt,
                    ownership = ownership,
                    permissions = permissions
            )
            MMRef.Type.SYMLINK -> SymlinkProps(
                    originalPath = lookedUp,
                    modifiedAt = modifiedAt,
                    ownership = ownership,
                    symlinkTarget = target!!
            )
            MMRef.Type.ARCHIVE -> throw UnsupportedOperationException("Pathlookups can't be direct archives.")
        }
    }

    companion object {
        val TAG = App.logTag("MMDataRepo", "BaseRefSource")
    }
}