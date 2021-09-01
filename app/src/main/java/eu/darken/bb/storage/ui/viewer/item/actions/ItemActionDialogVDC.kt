package eu.darken.bb.storage.ui.viewer.item.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.Bugs
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.CAString
import eu.darken.bb.common.Operation
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.common.intro.IntroFragmentVDC
import io.reactivex.rxjava3.schedulers.Schedulers

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

    val actionEvent = SingleLiveEvent<Triple<ItemAction, Storage.Id, BackupSpec.Id>>()
    val errorEvents = SingleLiveEvent<Throwable>()
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageObs.flatMap { it.specInfos() }
            .map { contents -> contents.single { it.backupSpec.specId == backupSpecId } }
            .take(1)
            .subscribe({ content ->
                stater.update { it.copy(info = content) }
            }, { error ->
                errorEvents.postValue(error)
                finishedEvent.postValue(Any())
            })
            .withScopeVDC(this)

        storageObs.flatMap { it.info() }
            .filter { it.isFinished }
            .take(1)
            .subscribe({ info ->
                val actions = mutableListOf<Confirmable<ItemAction>>().apply {
                    add(Confirmable(ItemAction.VIEW))
                    add(Confirmable(ItemAction.RESTORE))
                    if (info.status?.isReadOnly == false) {
                        add(Confirmable(ItemAction.DELETE, requiredLvl = 1))
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
                    .flatMapSingle { it.remove(backupSpecId) }
                    .subscribeOn(Schedulers.io())
                    .doOnError { Bugs.track(it) }
                    .doFinally { stater.update { it.copy(currentOp = null) } }
                    .doOnSubscribe { disp ->
                        stater.update {
                            it.copy(
                                currentOp = Operation(
                                    CAString(R.string.progress_deleting_label),
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
            ItemAction.RESTORE -> taskBuilder.createEditor(type = Task.Type.RESTORE_SIMPLE)
                .subscribeOn(Schedulers.io())
                .flatMap { data ->
                    (data.editor as SimpleRestoreTaskEditor).addBackupSpecId(storageId, backupSpecId)
                        .map { data.taskId }
                }
                .flatMapCompletable { taskBuilder.startEditor(it) }
                .doOnError { Bugs.track(it) }
                .doFinally { stater.update { it.copy(currentOp = null) } }
                .doOnSubscribe { disp ->
                    stater.update {
                        it.copy(currentOp = Operation(CAString(R.string.progress_loading_label), disposable = disp))
                    }
                }
                .subscribe(
                    { finishedEvent.postValue(Any()) },
                    { error -> errorEvents.postValue(error) }
                )
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

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id): ItemActionDialogVDC
    }
}