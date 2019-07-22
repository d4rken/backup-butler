package eu.darken.bb.repos.core.local

import eu.darken.bb.common.file.SFile
import eu.darken.bb.repos.core.BackupRepo
import eu.darken.bb.repos.core.RepoRef
import java.util.*

data class LocalStorageRepoRef(
        val path: SFile,
        override val repoId: UUID = UUID.randomUUID()
) : RepoRef {

    override val repoType: BackupRepo.Type = BackupRepo.Type.LOCAL_STORAGE

}