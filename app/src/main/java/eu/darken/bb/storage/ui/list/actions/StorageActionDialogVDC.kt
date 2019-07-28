package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.tasks.ui.editor.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers
import java.util.*

class StorageActionDialogVDC @AssistedInject constructor(
        private val storageManager: StorageManager,
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: UUID
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
                TODO()
            }
            DELETE -> {
                TODO()
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