package eu.darken.bb.storage.core.local

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Reusable
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.storage.core.Storage
import javax.inject.Inject

@Reusable
class LocalStorageFactory @Inject constructor(
        @AppContext private val context: Context,
        private val moshi: Moshi,
        private val localStorageEditorFactory: LocalStorageEditor.Factory,
        private val tmpDataRepo: TmpDataRepo
) : Storage.Factory {

    override fun isCompatible(storageRef: Storage.Ref): Boolean {
        return storageRef.storageType == Storage.Type.LOCAL
    }

    override fun create(storageRef: Storage.Ref, progressClient: Progress.Client?): Storage {
        return LocalStorage(
                context,
                moshi,
                localStorageEditorFactory,
                tmpDataRepo,
                storageRef as LocalStorageRef,
                progressClient
        )
    }

}