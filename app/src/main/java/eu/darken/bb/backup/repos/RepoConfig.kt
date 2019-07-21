package eu.darken.bb.backup.repos

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.repos.local.LocalStorageConfig

interface RepoConfig {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RepoConfig> = PolymorphicJsonAdapterFactory.of(RepoConfig::class.java, "repoType")
                .withSubtype(LocalStorageConfig::class.java, BackupRepo.Type.LOCAL_STORAGE.name)
    }

    val repoType: BackupRepo.Type
}