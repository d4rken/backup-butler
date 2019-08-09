package eu.darken.bb.storage.ui.viewer.content.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.StateUpdater
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.ui.editor.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers

class ContentActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        private val storageManager: StorageManager
) : SmartVDC() {

    private val stateUpdater = StateUpdater(State(loading = true))
    val state = stateUpdater.state

    init {
        storageManager.info(storageId)
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .subscribe(
                        { storageInfo ->
                            val allowedActions = mutableSetOf<ContentAction>()
                            allowedActions.add(ContentAction.VIEW)
                            allowedActions.add(ContentAction.DELETE)

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

    fun storageAction(action: ContentAction) {
        when (action) {
            ContentAction.VIEW -> {
                TODO()
            }

            ContentAction.DELETE -> {
                TODO()
            }
        }
    }

    data class State(
            val loading: Boolean = false,
            val finished: Boolean = false,
            val storageInfo: StorageInfo? = null,
            val allowedActions: List<ContentAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ContentActionDialogVDC
    }
}