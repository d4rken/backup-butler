package eu.darken.bb.backup.repos

import eu.darken.bb.backup.backups.BackupId
import java.io.File
import java.util.*

data class DefaultRevisionConfig(
        override val revisions: List<Revision>
) : BackupRepo.RevisionConfig {

    override fun getRevision(backupId: BackupId): Revision? = revisions.find { it.backupId == backupId }

    override val revisionType = BackupRepo.RevisionConfig.Type.SIMPLE

    data class Revision(
            override val backupId: BackupId,
            override val createdAt: Date
    ) : BackupRepo.RevisionConfig.Revision {

        fun getRevDir(base: File): File = File(base, backupId.toString())
    }
}