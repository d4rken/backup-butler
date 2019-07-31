package eu.darken.bb.storage.ui.list

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import timber.log.Timber
import java.util.*

class StorageListFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder,
        @AppContext private val context: Context
) : SmartVDC() {

    val viewState: LiveData<ViewState> = Observables
            .combineLatest(storageManager.info(), Observable.just(""))
            .map { (repos, _) ->
                return@map ViewState(
                        storages = repos.toList()
                )
            }
            .toLiveData()

    val editTaskEvent = SingleLiveEvent<EditActions>()

    init {

    }

    fun createStorage() {
        storageBuilder.startEditor()
    }

    fun editStorage(item: StorageInfo) {
        Timber.tag(TAG).d("editStorage(%s)", item)
        editTaskEvent.postValue(EditActions(
                storageId = item.ref.storageId,
                allowView = true,
                allowEdit = true,
                allowDelete = true
        ))
    }

    data class ViewState(
            val storages: List<StorageInfo>
    )

    data class EditActions(
            val storageId: UUID,
            val allowView: Boolean = false,
            val allowEdit: Boolean = false,
            val allowDelete: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<StorageListFragmentVDC>
}