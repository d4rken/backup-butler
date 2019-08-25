package eu.darken.bb.storage.ui.viewer.content.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityVDC
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers

class ContentActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId)
            .subscribeOn(Schedulers.io())

    private val stater = Stater(State())
    val state = stater.liveData

    val pageEvent = SingleLiveEvent<StorageViewerActivityVDC.PageData>()
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageObs.flatMap { it.content() }
                .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
                .subscribe({ content ->
                    stater.update {
                        it.copy(content = content, workIds = it.clearWorkId(WorkId.ID1))
                    }
                }, {
                    finishedEvent.postValue(Any())
                })
                .withScopeVDC(this)

        storageObs.flatMap { it.info() }
                .subscribe({ info ->
                    val actions = ContentAction.values().toMutableList().apply {
                        if (info.status?.isReadOnly == true) remove(ContentAction.DELETE)
                    }.toList()
                    stater.update {
                        it.copy(allowedActions = actions, workIds = it.clearWorkId(WorkId.ID2))
                    }
                }, {
                    finishedEvent.postValue(Any())
                })
                .withScopeVDC(this)
    }

    fun storageAction(action: ContentAction) {
        when (action) {
            ContentAction.VIEW -> {
                pageEvent.postValue(StorageViewerActivityVDC.PageData(StorageViewerActivityVDC.PageData.Page.DETAILS, storageId, backupSpecId))
            }
            ContentAction.DELETE -> {
                storageObs
                        .flatMapSingle { it.remove(backupSpecId) }
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stater.update { it.copy(workIds = it.addWorkId("deletion")) } }
                        .doFinally { finishedEvent.postValue(Any()) }
                        .subscribe()
            }
            ContentAction.RESTORE -> TODO()
        }
    }

    data class State(
            val content: Storage.Content? = null,
            val allowedActions: List<ContentAction> = listOf(),
            override val workIds: Set<WorkId> = setOf(WorkId.ID1, WorkId.ID2)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ContentActionDialogVDC
    }
}