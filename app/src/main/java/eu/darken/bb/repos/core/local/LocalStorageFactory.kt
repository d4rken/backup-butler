package eu.darken.bb.repos.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Reusable
import eu.darken.bb.backup.processor.tmp.TmpDataRepo
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.repos.core.BackupRepo
import eu.darken.bb.repos.core.RepoRef
import javax.inject.Inject

@Reusable
class LocalStorageFactory @Inject constructor(
        @AppContext private val context: Context,
        private val moshi: Moshi,
        private val tmpDataRepo: TmpDataRepo
) : BackupRepo.Factory {

    override fun isCompatible(repoRef: RepoRef): Boolean {
        return repoRef.repoType == BackupRepo.Type.LOCAL_STORAGE
    }

    override fun create(repoRef: RepoRef): BackupRepo {
        repoRef as LocalStorageRepoRef
        return LocalStorageRepo(context, moshi, tmpDataRepo, repoRef)
    }

}