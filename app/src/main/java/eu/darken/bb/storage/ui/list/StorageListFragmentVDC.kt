package eu.darken.bb.storage.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import timber.log.Timber

class StorageListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder
) : SmartVDC() {

    val viewState: LiveData<ViewState> = Observables
            .combineLatest(storageManager.infos(), Observable.just(""))
            .map { (storages, _) ->
                return@map ViewState(
                        storages = storages.map { StorageInfoOpt(it) }
                )
            }
            .toLiveData()

    val editTaskEvent = SingleLiveEvent<EditActions>()

    fun createStorage() {
        storageBuilder.startEditor()
    }

    fun editStorage(item: StorageInfoOpt) {
        Timber.tag(TAG).d("editStorage(%s)", item)
        editTaskEvent.postValue(EditActions(
                storageId = item.storageId,
                allowView = true,
                allowEdit = true,
                allowDelete = true
        ))
    }

    data class ViewState(
            val storages: List<StorageInfoOpt>
    )

    data class EditActions(
            val storageId: BackupStorage.Id,
            val allowView: Boolean = false,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<StorageListFragmentVDC>
}