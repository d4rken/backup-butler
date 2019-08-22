package eu.darken.bb.storage.ui.viewer.content.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityVDC
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentVDC
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ContentActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId).subscribeOn(Schedulers.io())

    private val stater = Stater(State())

    val state = stater.liveData
    val pageEvent = SingleLiveEvent<StorageViewerActivityVDC.PageData>()
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageObs.flatMapObservable { it.content() }
                .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
                .doOnNext { content ->
                    stater.update {
                        it.copy(
                                content = content,
                                workId = it.tryClearWorkId(),
                                allowedActions = ContentAction.values().toList()
                        )
                    }
                }
                .doOnError {
                    finishedEvent.postValue(Any())
                }
                .onErrorResumeNext(Observable.empty())
                .withStater(stater)
    }

    fun storageAction(action: ContentAction) {
        when (action) {
            ContentAction.VIEW -> {
                pageEvent.postValue(StorageViewerActivityVDC.PageData(StorageViewerActivityVDC.PageData.Page.DETAILS, storageId, backupSpecId))
            }
            ContentAction.DELETE -> {
                val workId = WorkId()
                storageObs
                        .flatMap { it.remove(backupSpecId) }
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stater.update { it.copy(workId = workId) } }
                        .doFinally { finishedEvent.postValue(Any()) }
                        .subscribe()
            }
            ContentAction.RESTORE -> TODO()
        }
    }

    data class State(
            val content: Storage.Content? = null,
            val allowedActions: List<ContentAction> = listOf(),
            override val workId: WorkId = WorkId.DEFAULT
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ContentActionDialogVDC
    }
}