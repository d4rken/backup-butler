package eu.darken.bb.processor.core.mm

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.SFile
import java.io.File
import java.util.*

data class MMRef(
        val refId: Id,
        val backupId: Backup.Id,
        val type: Type,
        val tmpPath: File,
        val originalPath: SFile
) {

    val props: Props
        get() {
            return Props(
                    originalPath = originalPath,
                    refType = type
            )
        }

    data class Props(
            val originalPath: SFile,
            val refType: Type
    )

    enum class Type {
        FILE, DIRECTORY
    }

    data class Id(val id: UUID = UUID.randomUUID()) {

        val idString = id.toString()

        override fun toString(): String = "MMRef.Id($idString)"
    }
}

fun SFile.Type.toMMRefType(): MMRef.Type {
    return when (this) {
        SFile.Type.FILE -> MMRef.Type.FILE
        SFile.Type.DIRECTORY -> MMRef.Type.DIRECTORY
    }
}