package eu.darken.bb.processor.core.mm

import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.file.AFile
import java.io.File
import java.util.*

data class MMRef(
        val refId: Id,
        val backupId: Backup.Id,
        val type: Type,
        val tmpPath: File,
        val originalPath: AFile
) {

    val props: Props
        get() {
            return Props(
                    originalPath = originalPath,
                    refType = type
            )
        }

    data class Props(
            val originalPath: AFile,
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

fun AFile.Type.toMMRefType(): MMRef.Type {
    return when (this) {
        AFile.Type.FILE -> MMRef.Type.FILE
        AFile.Type.DIRECTORY -> MMRef.Type.DIRECTORY
    }
}