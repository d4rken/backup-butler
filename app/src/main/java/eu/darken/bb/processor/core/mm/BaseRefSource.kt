package eu.darken.bb.processor.core.mm

import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.APathLookup
import eu.darken.bb.common.files.core.toMMRefType
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import eu.darken.bb.processor.core.mm.generic.FileProps
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import okio.Source
import timber.log.Timber
import java.io.Closeable

abstract class BaseRefSource : MMRef.RefSource {

    internal val resources = mutableSetOf<Closeable>()
    internal var isReleased = false

    override suspend fun open(): Source {
        require(!isReleased) { "Don't call open() on a released resource!" }
        return doOpen().also { resources.add(it) }
    }

    abstract suspend fun doOpen(): Source

    override suspend fun release() {
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
        val TAG = logTag("MMDataRepo", "BaseRefSource")

        fun APathLookup<APath>.toProps(label: String? = null): Props {
            return when (fileType.toMMRefType()) {
                MMRef.Type.FILE -> FileProps(
                    label = label,
                    originalPath = lookedUp,
                    modifiedAt = modifiedAt,
                    ownership = ownership,
                    permissions = permissions
                )
                MMRef.Type.DIRECTORY -> DirectoryProps(
                    label = label,
                    originalPath = lookedUp,
                    modifiedAt = modifiedAt,
                    ownership = ownership,
                    permissions = permissions
                )
                MMRef.Type.SYMLINK -> SymlinkProps(
                    label = label,
                    originalPath = lookedUp,
                    modifiedAt = modifiedAt,
                    ownership = ownership,
                    symlinkTarget = target!!
                )
                MMRef.Type.ARCHIVE -> throw UnsupportedOperationException("Pathlookups can't be direct archives.")
            }
        }
    }
}