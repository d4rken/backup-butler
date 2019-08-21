package eu.darken.bb.storage.ui.editor

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.ui.editor.types.TypeSelectionFragment
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragment
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class StorageEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageBuilder: StorageBuilder
) : SmartVDC() {

    private val dataObs = storageBuilder.storage(storageId)
            .subscribeOn(Schedulers.io())

    private val validObs: Observable<Boolean> = dataObs
            .switchMap { if (it.editor != null) it.editor.isValid() else Observable.just(false) }
            .doOnNext { isValid -> stater.update { it.copy(allowSave = isValid) } }

    private val existingObs: Observable<Boolean> = dataObs
            .map { if (it.editor != null) it.editor.isExistingStorage else false }
            .doOnNext { isExisting -> stater.update { it.copy(existing = isExisting) } }

    private val pageObs: Observable<StorageBuilder.Data> = dataObs.doOnNext { data ->
        val p = PageData(data.storageId, data.storageType)
        if (stater.snapshot.currentPage != p.getPage()) {
            pageEvent.postValue(p)
            stater.update { it.copy(currentPage = p.getPage()) }
        }
    }

    private val stater = Stater(State(storageId = storageId))
            .addLiveDep {
                validObs.subscribe()
                existingObs.subscribe()
                pageObs.subscribe()
            }

    val state = stater.liveData

    val pageEvent = SingleLiveEvent<PageData>()
    val finishActivity = SingleLiveEvent<Boolean>()

    init {
        dataObs.take(1).subscribe {
            stater.update { it.copy(isWorking = false) }
        }
    }

    fun saveConfig() {
        storageBuilder.save(storageId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(isWorking = true) } }
                .doFinally { finishActivity.postValue(true) }
                .subscribe()
    }

    data class State(
            val storageId: Storage.Id,
            val currentPage: PageData.Page? = null,
            val existing: Boolean = false,
            val allowSave: Boolean = false,
            val isWorking: Boolean = true
    )

    data class PageData(val storageId: Storage.Id, private val type: Storage.Type?) {

        fun getPage(): Page = Page.values().first { it.backupType == type }

        enum class Page(val backupType: Storage.Type?, val fragmentClass: KClass<out Fragment>) {
            SELECTION(null, TypeSelectionFragment::class),
            LOCAL(Storage.Type.LOCAL, LocalEditorFragment::class)
        }

    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageEditorActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageEditorActivityVDC
    }
}