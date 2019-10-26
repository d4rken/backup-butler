package eu.darken.bb.processor.core.mm

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.IdType
import eu.darken.bb.common.file.APath
import java.io.File
import java.util.*

data class MMRef(
        val refId: Id,
        val backupId: Backup.Id,
        val tmpPath: File,
        val originalPath: APath
) {

    val type: Type
        get() = when {
            tmpPath.isDirectory -> Type.DIRECTORY
            tmpPath.isFile -> Type.FILE
            !tmpPath.exists() -> Type.UNUSED
            else -> throw IllegalStateException("$tmpPath is neither file nor dir (exists=${tmpPath.exists()})")
        }

    val props: Props
        get() {
            return Props(
                    originalPath = originalPath,
                    refType = type
            )
        }

    data class Props(
            val originalPath: APath,
            val refType: Type
    )

    enum class Type {
        FILE, DIRECTORY, UNUSED
    }

    data class Id(override val value: UUID = UUID.randomUUID()) : IdType<Id> {

        override val idString = value.toString()

        override fun compareTo(other: Id): Int = value.compareTo(other.value)

        override fun toString(): String = "MMRef.Id($idString)"
    }
}