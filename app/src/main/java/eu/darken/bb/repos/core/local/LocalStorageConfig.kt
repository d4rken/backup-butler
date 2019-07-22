package eu.darken.bb.repos.core.local

import eu.darken.bb.repos.core.BackupRepo
import eu.darken.bb.repos.core.RepoConfig

data class LocalStorageConfig(
        override val label: String
) : RepoConfig {
    override val repoType: BackupRepo.Type = BackupRepo.Type.LOCAL_STORAGE
}