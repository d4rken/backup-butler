package eu.darken.bb.backup.repos.local

import eu.darken.bb.backup.repos.BackupRepo

class LocalStorageConfig : BackupRepo.RepoConfig {
    override val repoType: BackupRepo.Type = BackupRepo.Type.LOCAL_STORAGE
}