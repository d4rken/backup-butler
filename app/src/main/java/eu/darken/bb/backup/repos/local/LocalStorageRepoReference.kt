package eu.darken.bb.backup.repos.local

import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.RepoReference
import eu.darken.bb.common.file.SFile

data class LocalStorageRepoReference(
        val path: SFile
) : RepoReference {

    override val repoType: BackupRepo.Type = BackupRepo.Type.LOCAL_STORAGE

}