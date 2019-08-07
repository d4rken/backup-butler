package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.tasks.ui.editor.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class StorageActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: UUID,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder,
        private val storageRefRepo: StorageRefRepo
) : SmartVDC() {

    private val stateUpdater = StateUpdater(State(loading = true))
    val state = stateUpdater.state

    init {
        storageManager.info(storageId)
                .subscribeOn(Schedulers.io())
                .subscribe { maybeTask ->
                    val storage = maybeTask.value
                    stateUpdater.update {
                        if (storage == null) {
                            it.copy(loading = true, finished = true)
                        } else {
                            it.copy(
                                    storage = storage,
                                    loading = false,
                                    allowedActions = values().toList()
                            )
                        }
                    }
                }
    }

    fun storageAction(action: StorageAction) {
        when (action) {
            VIEW -> {
                TODO()
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
                        .subscribe { storage ->
                        }
            }
        }
    }

    data class State(
            val loading: Boolean = false,
            val finished: Boolean = false,
            val storage: StorageInfo? = null,
            val allowedActions: List<StorageAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: UUID): StorageActionDialogVDC
    }
}