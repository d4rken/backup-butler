package eu.darken.bb.storage.core

import eu.darken.bb.backup.core.Backup
import java.io.File
import java.util.*

data class DefaultRevisionConfig(
        override val revisions: List<Revision>
) : RevisionConfig {

    override fun getRevision(backupId: Backup.Id): Revision? = revisions.find { it.backupId == backupId }

    override val revisionType = RevisionConfig.Type.SIMPLE

    data class Revision(
            override val backupId: Backup.Id,
            override val createdAt: Date
    ) : RevisionConfig.Revision {

        fun getRevDir(base: File): File = File(base, backupId.toString())
    }
}