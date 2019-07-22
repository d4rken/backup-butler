package eu.darken.bb.repos.core

import eu.darken.bb.backup.backups.BackupId
import java.io.File
import java.util.*

data class DefaultRevisionConfig(
        override val revisions: List<Revision>
) : RevisionConfig {

    override fun getRevision(backupId: BackupId): Revision? = revisions.find { it.backupId == backupId }

    override val revisionType = RevisionConfig.Type.SIMPLE

    data class Revision(
            override val backupId: BackupId,
            override val createdAt: Date
    ) : RevisionConfig.Revision {

        fun getRevDir(base: File): File = File(base, backupId.toString())
    }
}