package eu.darken.bb.storage.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Reusable
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.processor.tmp.TmpDataRepo
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageRef
import javax.inject.Inject

@Reusable
class LocalStorageFactory @Inject constructor(
        @AppContext private val context: Context,
        private val moshi: Moshi,
        private val tmpDataRepo: TmpDataRepo
) : BackupStorage.Factory {

    override fun isCompatible(storageRef: StorageRef): Boolean {
        return storageRef.storageType == BackupStorage.Type.LOCAL_STORAGE
    }

    override fun create(storageRef: StorageRef): BackupStorage {
        storageRef as LocalStorageStorageRef
        return LocalStorageStorage(context, moshi, tmpDataRepo, storageRef)
    }

}