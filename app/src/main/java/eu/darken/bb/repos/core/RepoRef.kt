package eu.darken.bb.repos.core

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import eu.darken.bb.repos.core.local.LocalStorageRepoRef
import java.util.*

interface RepoRef {
    companion object {
        val MOSHI_FACTORY: PolymorphicJsonAdapterFactory<RepoRef> = PolymorphicJsonAdapterFactory.of(RepoRef::class.java, "repoType")
                .withSubtype(LocalStorageRepoRef::class.java, BackupRepo.Type.LOCAL_STORAGE.name)
    }

    val repoId: UUID
    val repoType: BackupRepo.Type
}
