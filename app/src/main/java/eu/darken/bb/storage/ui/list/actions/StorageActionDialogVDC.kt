package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.*
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.task.ui.editor.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class StorageActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder,
        private val storageRefRepo: StorageRefRepo
) : SmartVDC() {

    private val stateUpdater = StateUpdater(State(loading = true))
    val state = stateUpdater.state

    init {
        storageManager.info(storageId)
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .subscribe(
                        { storageInfo ->
                            val allowedActions = mutableSetOf<StorageAction>()
                            if (storageInfo != null) {
                                allowedActions.add(VIEW)
                                if (storageInfo.config != null) {
                                    allowedActions.add(EDIT)
                                } else {
                                    allowedActions.add(DELETE)
                                }
                            }
                            stateUpdater.update {
                                if (storageInfo == null) {
                                    it.copy(loading = true, finished = true)
                                } else {
                                    it.copy(
                                            storageInfo = storageInfo,
                                            loading = false,
                                            allowedActions = allowedActions.toList()
                                    )
                                }
                            }
                        },
                        {
                            stateUpdater.update { it.copy(finished = true) }
                        }
                )
    }

    fun storageAction(action: StorageAction) {
        when (action) {
            VIEW -> {
                storageManager.startViewer(storageId)
                stateUpdater.update { it.copy(loading = false, finished = true) }
            }
            EDIT -> {
                storageBuilder.load(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                        .subscribe { storage ->
                            storageBuilder.startEditor(storage.storageId)
                        }
            }
            DELETE -> {
                storageRefRepo.remove(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                        .subscribe()
            }
        }
    }

    data class State(
            val loading: Boolean = false,
            val finished: Boolean = false,
            val storageInfo: StorageInfo? = null,
            val allowedActions: List<StorageAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageActionDialogVDC
    }
}