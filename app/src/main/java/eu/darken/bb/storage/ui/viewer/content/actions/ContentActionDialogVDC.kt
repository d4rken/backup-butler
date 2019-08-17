package eu.darken.bb.storage.ui.viewer.content.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityVDC
import eu.darken.bb.task.ui.editor.intro.IntroFragmentVDC
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ContentActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())
    private val contentObs: Observable<Storage.Content> = storageObs.flatMapObservable { it.content() }
            .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
            .doOnNext { content ->
                val allowedActions = mutableSetOf<ContentAction>()
                allowedActions.add(ContentAction.VIEW)
                allowedActions.add(ContentAction.RESTORE)
                allowedActions.add(ContentAction.DELETE)

                stateUpdater.update {
                    it.copy(
                            content = content,
                            loading = false,
                            allowedActions = allowedActions.toList()
                    )
                }
            }
            .doOnError {
                stateUpdater.update { it.copy(finished = true) }
            }

    private val stateUpdater = Stater(State(loading = true))
            .addLiveDep { contentObs.subscribe() }

    val state = stateUpdater.liveData
    val pageEvent = SingleLiveEvent<StorageViewerActivityVDC.PageData>()

    fun storageAction(action: ContentAction) {
        when (action) {
            ContentAction.VIEW -> {
                pageEvent.postValue(StorageViewerActivityVDC.PageData(StorageViewerActivityVDC.PageData.Page.DETAILS, storageId, backupSpecId))
            }
            ContentAction.DELETE -> {
                TODO()
            }
        }
    }

    data class State(
            val loading: Boolean = false,
            val finished: Boolean = false,
            val content: Storage.Content? = null,
            val allowedActions: List<ContentAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ContentActionDialogVDC
    }
}