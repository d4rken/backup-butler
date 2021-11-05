package eu.darken.bb.storage.ui.viewer.item.actions

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.Bugs
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.Operation
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.toCaString
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ItemActionDialogVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    storageManager: StorageManager
) : SmartVDC() {

    private val navArgs by handle.navArgs<ItemActionDialogArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val backupSpecId: BackupSpec.Id = navArgs.specId

    private val storageObs = storageManager.getStorage(storageId).observeOn(Schedulers.computation())

    private val stater = Stater { State() }
    val state = stater.liveData

    val actionEvent = SingleLiveEvent<Triple<ItemAction, Storage.Id, BackupSpec.Id>>()
    val errorEvents = SingleLiveEvent<Throwable>()
    val navEvents = SingleLiveEvent<NavDirections>()
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageObs
            .flatMapObservable { it.specInfos() }
            .map { contents -> contents.single { it.backupSpec.specId == backupSpecId } }
            .take(1)
            .subscribe({ content ->
                stater.update { it.copy(info = content) }
            }, { error ->
                errorEvents.postValue(error)
                finishedEvent.postValue(Any())
            })
            .withScopeVDC(this)

        storageObs
            .flatMapObservable { it.info() }
            .filter { it.isFinished }
            .take(1)
            .subscribe({ info ->
                val actions = mutableListOf<Confirmable<ItemAction>>().apply {
                    add(Confirmable(ItemAction.VIEW) { storageAction(it) })
                    add(Confirmable(ItemAction.RESTORE) { storageAction(it) })
                    if (info.status?.isReadOnly == false) {
                        add(Confirmable(ItemAction.DELETE, requiredLvl = 1) { storageAction(it) })
                    }
                }.toList()
                stater.update { it.copy(allowedActions = actions) }
            }, { error ->
                errorEvents.postValue(error)
                finishedEvent.postValue(Any())
            })
            .withScopeVDC(this)
    }

    fun storageAction(action: ItemAction) {
        require(stater.snapshot.currentOp == null)

        when (action) {
            ItemAction.VIEW -> {
                actionEvent.postValue(Triple(ItemAction.VIEW, storageId, backupSpecId))
            }
            ItemAction.DELETE -> {
                storageObs
                    .flatMap { it.remove(backupSpecId) }
                    .doOnError { Bugs.track(it) }
                    .doFinally { stater.update { it.copy(currentOp = null) } }
                    .doOnSubscribe { disp ->
                        stater.update {
                            it.copy(
                                currentOp = Operation(
                                    R.string.progress_deleting_label.toCaString(),
                                    disposable = disp
                                )
                            )
                        }
                    }
                    .subscribe(
                        { finishedEvent.postValue(Any()) },
                        { error -> errorEvents.postValue(error) }
                    )
            }
            ItemAction.RESTORE -> taskBuilder.getEditor(type = Task.Type.RESTORE_SIMPLE)
                .flatMap { data ->
                    (data.editor as SimpleRestoreTaskEditor).addBackupSpecId(storageId, backupSpecId)
                        .map { data.taskId }
                }
                .doOnError { Bugs.track(it) }
                .doFinally { stater.update { it.copy(currentOp = null) } }
                .doOnSubscribe { disp ->
                    stater.update {
                        it.copy(currentOp = Operation(R.string.progress_loading_label.toCaString(), disposable = disp))
                    }
                }
                .subscribe({
                    ItemActionDialogDirections.actionStorageItemActionDialogToTaskEditor(
                        args = TaskEditorArgs(taskId = it, taskType = Task.Type.RESTORE_SIMPLE)
                    ).run { navEvents.postValue(this) }
                    finishedEvent.postValue(Any())
                }, { error ->
                    errorEvents.postValue(error)
                })
                .withScopeVDC(this)
        }
    }

    data class State(
        val info: BackupSpec.Info? = null,
        val allowedActions: List<Confirmable<ItemAction>>? = null,
        val currentOp: Operation? = null
    ) {
        val isWorking: Boolean
            get() = currentOp != null || allowedActions == null
    }
}