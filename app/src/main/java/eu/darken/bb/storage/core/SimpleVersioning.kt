package eu.darken.bb.storage.core

import eu.darken.bb.backup.core.Backup
import java.io.File
import java.util.*

data class SimpleVersioning(
        override val versions: List<Version> = emptyList()
) : Versioning {

    override fun getVersion(backupId: Backup.Id): Version? = versions.find { it.backupId == backupId }

    override val versioningType = Versioning.Type.SIMPLE

    data class Version(
            override val backupId: Backup.Id,
            override val createdAt: Date
    ) : Versioning.Version {

        fun getRevDir(base: File): File = File(base, backupId.id.toString())
    }
}