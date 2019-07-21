package eu.darken.bb.backup.repos.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Reusable
import eu.darken.bb.backup.processor.tmp.TmpDataRepo
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.RepoReference
import eu.darken.bb.common.dagger.AppContext
import javax.inject.Inject

@Reusable
class LocalStorageFactory @Inject constructor(
        @AppContext private val context: Context,
        private val moshi: Moshi,
        private val tmpDataRepo: TmpDataRepo
) : BackupRepo.Factory {

    override fun isCompatible(repoReference: RepoReference): Boolean {
        return repoReference.repoType == BackupRepo.Type.LOCAL_STORAGE
    }

    override fun create(repoReference: RepoReference): BackupRepo {
        repoReference as LocalStorageRepoReference
        return LocalStorageRepo(context, moshi, tmpDataRepo, repoReference)
    }

}