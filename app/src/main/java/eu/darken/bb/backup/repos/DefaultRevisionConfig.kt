package eu.darken.bb.backup.repos

import eu.darken.bb.backup.backups.BackupId
import java.io.File
import java.util.*

data class DefaultRevisionConfig(
        override val revisions: List<Revision>
) : BackupRepo.RevisionConfig {

    override val configType = BackupRepo.RevisionConfig.Type.SIMPLE

    data class Revision(
            override val id: BackupId,
            override val createdAt: Date
    ) : BackupRepo.RevisionConfig.Revision {

        fun getRevDir(base: File): File = File(base, id.toString())
    }
}