package eu.darken.bb.storage.ui.viewer

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragment
import eu.darken.bb.storage.ui.viewer.content.StorageContentFragment
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class StorageViewerActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.info(storageId).subscribeOn(Schedulers.io())
            .doOnNext { info ->
                stateUpdater.update { state ->
                    state.copy(
                            page = State.Page.CONTENT,
                            storageId = storageId,
                            label = info.config?.label ?: ""
                    )
                }
            }
            .doOnError { error ->
                stateUpdater.update {
                    it.copy(error = error)
                }
                finishActivity.postValue(true)
            }
            .onErrorResumeNext(Observable.empty())
            .firstOrError()


    private val stateUpdater: StateUpdater<State> = StateUpdater(State(storageId = storageId))
            .addLiveDep {
                storageObs.subscribe()
            }

    val finishActivity = SingleLiveEvent<Boolean>()

    val state = stateUpdater.state

    data class State(
            val storageId: Storage.Id,
            val page: Page = Page.CONTENT,
            val label: String = "",
            val error: Throwable? = null
    ) {
        enum class Page(
                val fragmentClass: KClass<out Fragment>
        ) {
            CONTENT(StorageContentFragment::class),
            DETAILS(LocalEditorFragment::class)
        }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageViewerActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageViewerActivityVDC
    }
}