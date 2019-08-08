package eu.darken.bb.storage.core

import eu.darken.bb.backup.core.Backup
import java.io.File
import java.util.*

data class DefaultVersioning(
        override val versions: List<Version>
) : Versioning {

    override fun getRevision(backupId: Backup.Id): Version? = versions.find { it.backupId == backupId }

    override val versioningType = Versioning.Type.SIMPLE

    data class Version(
            override val backupId: Backup.Id,
            override val createdAt: Date
    ) : Versioning.Version {

        fun getRevDir(base: File): File = File(base, backupId.toString())
    }
}