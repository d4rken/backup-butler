package eu.darken.bb.repos.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.repos.core.local.LocalStorageConfig

interface RepoConfig {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RepoConfig> = PolymorphicJsonAdapterFactory.of(RepoConfig::class.java, "repoType")
                .withSubtype(LocalStorageConfig::class.java, BackupRepo.Type.LOCAL_STORAGE.name)
    }

    val label: String
    val repoType: BackupRepo.Type
}