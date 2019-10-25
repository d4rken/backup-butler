package eu.darken.bb.storage.ui.viewer.item.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.Bugs
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.*
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers

class ItemActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        private val taskBuilder: TaskBuilder,
        storageManager: StorageManager
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId)
            .subscribeOn(Schedulers.io())

    private val stater = Stater(State())
    val state = stater.liveData

    val pageEvent = SingleLiveEvent<StorageViewerActivityVDC.PageData>()
    val errorEvents = SingleLiveEvent<Throwable>()
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageObs.flatMap { it.specInfos() }
                .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
                .subscribe({ content ->
                    stater.update {
                        it.copy(info = content, workIds = it.clearWorkId(WorkId.ID1))
                    }
                }, { error ->
                    errorEvents.postValue(error)
                    finishedEvent.postValue(Any())
                })
                .withScopeVDC(this)

        storageObs.flatMap { it.info() }
                .subscribe({ info ->
                    val actions = ItemAction.values().toMutableList().apply {
                        if (info.status?.isReadOnly == true) remove(ItemAction.DELETE)
                    }.toList()
                    stater.update {
                        it.copy(allowedActions = actions, workIds = it.clearWorkId(WorkId.ID2))
                    }
                }, { error ->
                    errorEvents.postValue(error)
                    finishedEvent.postValue(Any())
                })
                .withScopeVDC(this)
    }

    fun storageAction(action: ItemAction) {
        when (action) {
            ItemAction.VIEW -> {
                pageEvent.postValue(StorageViewerActivityVDC.PageData(StorageViewerActivityVDC.PageData.Page.DETAILS, storageId, backupSpecId))
            }
            ItemAction.DELETE -> {
                storageObs
                        .flatMapSingle { it.remove(backupSpecId) }
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stater.update { it.copy(workIds = it.addWorkId()) } }
                        .doFinally { stater.update { it.copy(workIds = it.clearWorkId()) } }
                        .doOnError { Bugs.track(it) }
                        .subscribe(
                                { finishedEvent.postValue(Any()) },
                                { error -> errorEvents.postValue(error) }
                        )
            }
            ItemAction.RESTORE -> taskBuilder.createEditor(type = Task.Type.RESTORE_SIMPLE)
                    .subscribeOn(Schedulers.io())
                    .flatMap { data ->
                        (data.editor as SimpleRestoreTaskEditor).addBackupSpecId(storageId, backupSpecId).map { data.taskId }
                    }
                    .flatMapCompletable { taskBuilder.startEditor(it) }
                    .doOnSubscribe { stater.update { it.copy(workIds = it.addWorkId()) } }
                    .doFinally { stater.update { it.copy(workIds = it.clearWorkId()) } }
                    .doOnError { Bugs.track(it) }
                    .subscribe(
                            { finishedEvent.postValue(Any()) },
                            { error -> errorEvents.postValue(error) }
                    )
                    .withScopeVDC(this)
        }
    }

    data class State(
            val info: BackupSpec.Info? = null,
            val allowedActions: List<ItemAction> = listOf(),
            override val workIds: Set<WorkId> = setOf(WorkId.ID1, WorkId.ID2)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ItemActionDialogVDC
    }
}