package eu.darken.bb.storage.ui.viewer

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.Stater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.content.StorageContentFragment
import eu.darken.bb.storage.ui.viewer.details.ContentDetailsFragment
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class StorageViewerActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        storageManager: StorageManager
) : SmartVDC() {
    private val storageInfoObs = storageManager.info(storageId).subscribeOn(Schedulers.io())
            .doOnNext { info ->
                stater.update { state ->
                    state.copy(
                            storageId = storageId,
                            label = info.config?.label ?: ""
                    )
                }
            }
            .doOnError { error ->
                stater.update {
                    it.copy(error = error)
                }
                finishActivity.postValue(true)
            }
            .onErrorResumeNext(Observable.empty())

    val pageEvent = SingleLiveEvent<PageData>()
    private val stater: Stater<State> = Stater {
        pageEvent.postValue(PageData(page = PageData.Page.CONTENT, storageId = storageId))
        State(storageId = storageId)
    }
            .addLiveDep {
                storageInfoObs.subscribe()
            }

    val finishActivity = SingleLiveEvent<Boolean>()
    val state = stater.liveData

    fun goTo(pageData: PageData) {
        pageEvent.postValue(pageData)
    }

    data class State(
            val storageId: Storage.Id,
            val label: String = "",
            val error: Throwable? = null,
            val loading: Boolean = true
    )

    data class PageData(
            val page: Page,
            val storageId: Storage.Id,
            val backupSpecId: BackupSpec.Id? = null
    ) {
        enum class Page(val fragmentClass: KClass<out Fragment>) {
            CONTENT(StorageContentFragment::class),
            DETAILS(ContentDetailsFragment::class)
        }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageViewerActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageViewerActivityVDC
    }
}