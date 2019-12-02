package eu.darken.bb.processor.core.mm

import eu.darken.bb.App
import eu.darken.bb.common.file.core.APath
import eu.darken.bb.common.file.core.APathGateway
import eu.darken.bb.common.file.core.APathLookup
import eu.darken.bb.common.file.core.toMMRefType
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


class FileRefSource(
        private val file: File,
        override val props: MMRef.Props
) : BaseRefSource({ file.source() }) {

//    override val props: MMRef.Props by lazy {
//        val dataType = file.getAPathFileType().toMMRefType()
//        val symlinkTarget: APath? = if (dataType == MMRef.Type.SYMBOLIC_LINK) {
//            LocalPath.build(file.canonicalFile)
//        } else {
//            null
//        }
//        MMRef.Props(
//                originalPath = LocalPath.build(file),
//                dataType = dataType,
//                symlinkTarget = symlinkTarget
//        )
//    }

}

class APathRefResource<PathType : APath, GateType : APathGateway<in PathType, out APathLookup<PathType>>>(
        private val path: PathType,
        private val gateway: GateType,
        providedProps: MMRef.Props? = null
) : BaseRefSource({ gateway.read(path) }) {

    private val autoGenProps: MMRef.Props by lazy {
        val lookup = gateway.lookup(path)
        val dataType = lookup.fileType.toMMRefType()
        val symlinkTarget: APath? = if (dataType == MMRef.Type.SYMBOLIC_LINK) {
            gateway.lookup(path).target
        } else {
            null
        }
        MMRef.Props(
                originalPath = path,
                dataType = dataType,
                modifiedAt = lookup.modifiedAt,
                ownership = lookup.ownership,
                permissions = lookup.permissions,
                symlinkTarget = symlinkTarget
        )
    }


    override val props: MMRef.Props = providedProps ?: autoGenProps

}