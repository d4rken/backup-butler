package eu.darken.bb.backup.repos.local

import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.common.file.SFile

data class LocalStorageRepoReference(val path: SFile) : BackupRepo.RepoReference {
    override val repoType: BackupRepo.Type
        get() = BackupRepo.Type.LOCAL_STORAGE
}