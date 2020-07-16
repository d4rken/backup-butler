package eu.darken.bb.storage.ui.editor

import androidx.annotation.IdRes
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.R
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import io.reactivex.rxjava3.schedulers.Schedulers


class StorageEditorActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageBuilder: StorageBuilder
) : SmartVDC() {

    private val storageObs = storageBuilder.storage(storageId)
            .subscribeOn(Schedulers.io())

    private val editorObs = storageObs
            .filter { it.editor != null }
            .map { it.editor!! }


    val finishEvent = SingleLiveEvent<Any>()

    private val stater = Stater {
        val data = storageObs.blockingFirst()
        val steps = when (data.storageType) {
            Storage.Type.LOCAL -> StepFlow.LOCAL
            Storage.Type.SAF -> StepFlow.SAF
            else -> StepFlow.SELECTION
        }
        State(storageId = storageId, storageType = data.storageType, stepFlow = steps)
    }
    val state = stater.liveData

    init {
        editorObs
                .flatMap { it.editorData }
                .subscribe { data ->
                    stater.update { it.copy(isExisting = data.existingStorage) }
                }
                .withScopeVDC(this)
    }

    fun dismiss() {
        storageBuilder.remove(storageId)
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    finishEvent.postValue(Any())
                }
    }

    data class State(
            val storageId: Storage.Id,
            val storageType: Storage.Type?,
            val isExisting: Boolean = false,
            val stepFlow: StepFlow
    )

    enum class StepFlow(@IdRes val start: Int) {
        SELECTION(R.id.typeSelectionFragment),
        LOCAL(R.id.localEditorFragment),
        SAF(R.id.safEditorFragment);
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<StorageEditorActivityVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageEditorActivityVDC
    }
}