package eu.darken.bb.processor.core.mm

import androidx.annotation.Keep
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.Ownership
import eu.darken.bb.common.files.core.Permissions
import eu.darken.bb.common.moshi.MyPolymorphicJsonAdapterFactory
import eu.darken.bb.processor.core.mm.archive.ArchiveProps
import eu.darken.bb.processor.core.mm.generic.DirectoryProps
import eu.darken.bb.processor.core.mm.generic.FileProps
import eu.darken.bb.processor.core.mm.generic.SymlinkProps
import java.util.*

@Keep
interface Props {
    val dataType: MMRef.Type
    val label: String?
    val originalPath: APath?

    val tryLabel: String
        get() = (label ?: originalPath?.path)!!

    interface HasModifiedDate {
        val modifiedAt: Date
    }

    interface HasPermissions {
        val permissions: Permissions?
    }

    interface HasOwner {
        val ownership: Ownership?
    }

    companion object {
        val MOSHI_FACTORY: MyPolymorphicJsonAdapterFactory<Props> =
            MyPolymorphicJsonAdapterFactory.of(Props::class.java, "dataType")
                .withSubtype(FileProps::class.java, MMRef.Type.FILE.name)
                .withSubtype(SymlinkProps::class.java, MMRef.Type.SYMLINK.name)
                .withSubtype(DirectoryProps::class.java, MMRef.Type.DIRECTORY.name)
                .withSubtype(ArchiveProps::class.java, MMRef.Type.ARCHIVE.name)
                .skipLabelSerialization()
    }
}

