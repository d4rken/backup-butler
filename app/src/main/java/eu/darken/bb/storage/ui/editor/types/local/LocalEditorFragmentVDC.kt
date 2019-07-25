package eu.darken.bb.storage.ui.editor.types.local

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.main.core.service.BackupService
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables

class LocalEditorFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val storageManager: StorageManager,
        @AppContext private val context: Context
) : SmartVDC() {

    val viewState: LiveData<ViewState> = Observables
            .combineLatest(storageManager.status(), Observable.just(""))
            .map { (repos, _) ->
                return@map ViewState(
                        storages = repos.toList()
                )
            }
            .toLiveData()

    init {

    }

    fun test() {
        context.startService(Intent(context, BackupService::class.java))
    }

    data class ViewState(
            val storages: List<StorageInfo>
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<LocalEditorFragmentVDC>
}