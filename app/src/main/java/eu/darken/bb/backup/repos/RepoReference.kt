package eu.darken.bb.backup.repos

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.backup.repos.local.LocalStorageRepoReference

interface RepoReference {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RepoReference> = PolymorphicJsonAdapterFactory.of(RepoReference::class.java, "repoType")
                .withSubtype(LocalStorageRepoReference::class.java, BackupRepo.Type.LOCAL_STORAGE.name)
    }

    val repoType: BackupRepo.Type
}