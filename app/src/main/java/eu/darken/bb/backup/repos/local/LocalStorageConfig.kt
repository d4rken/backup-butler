package eu.darken.bb.backup.repos.local

import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.RepoConfig

class LocalStorageConfig : RepoConfig {
    override val repoType: BackupRepo.Type = BackupRepo.Type.LOCAL_STORAGE
}