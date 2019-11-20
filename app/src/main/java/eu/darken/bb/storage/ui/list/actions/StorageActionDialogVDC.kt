package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.Bugs
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentVDC
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class StorageActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder,
        private val taskBuilder: TaskBuilder
) : SmartVDC() {

    private val stater = Stater(State(isLoadingData = true))
    val state = stater.liveData
    val closeDialogEvent = SingleLiveEvent<Any>()
    val errorEvent = SingleLiveEvent<Throwable>()

    init {
        storageManager.infos(listOf(storageId))
                .subscribeOn(Schedulers.io())
                .map { it.single() }
                .takeUntil { info -> info.isFinished }
                .subscribe { infoOpt ->
                    val allowedActions = mutableSetOf<Confirmable<StorageAction>>().apply {
                        if (infoOpt.info?.status != null) {
                            add(Confirmable(VIEW))
                            add(Confirmable(RESTORE))
                        }

                        if (infoOpt.info?.config != null) {
                            add(Confirmable(EDIT))
                        }
                        if (infoOpt.info?.status?.isReadOnly == false) {
                            add(Confirmable(DELETE, requiredLvl = 2))
                        }

                        add(Confirmable(DETACH, requiredLvl = 1))
                    }

                    stater.update {
                        it.copy(
                                storageInfo = infoOpt?.info,
                                allowedActions = allowedActions.toList(),
                                isLoadingData = !infoOpt.isFinished
                        )
                    }

                    if (infoOpt.anyError != null) {
                        errorEvent.postValue(infoOpt.error)
                    }
                }
                .withScopeVDC(this)
    }

    fun storageAction(action: StorageAction) {
        require(stater.snapshot.currentOperation == null)

        when (action) {
            VIEW -> {
                storageManager.startViewer(storageId)
                        .doFinally { stater.update { it.copy(currentOperation = null) } }
                        .doOnSubscribe { disp ->
                            stater.update { it.copy(currentOperation = disp) }
                        }
                        .doOnError { Bugs.track(it) }
                        .doFinally { stater.update { it.copy(currentOperation = null) } }
                        .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                        .subscribe(
                                { closeDialogEvent.postValue(Any()) },
                                { errorEvent.postValue(it) }
                        )
                        .withScopeVDC(this)
            }
            EDIT -> {
                storageBuilder.load(storageId)
                        .subscribeOn(Schedulers.io())
                        .flatMapCompletable { storageBuilder.startEditor(it.storageId) }
                        .doOnError { Bugs.track(it) }
                        .doFinally { stater.update { it.copy(currentOperation = null) } }
                        .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                        .subscribe(
                                { closeDialogEvent.postValue(Any()) },
                                { errorEvent.postValue(it) }
                        )
                        .withScopeVDC(this)
            }
            RESTORE -> {
                taskBuilder.createEditor(type = Task.Type.RESTORE_SIMPLE)
                        .subscribeOn(Schedulers.io())
                        .flatMap { data ->
                            (data.editor as SimpleRestoreTaskEditor).addStorageId(storageId).map { data.taskId }
                        }
                        .flatMapCompletable { taskBuilder.startEditor(it) }
                        .doOnError { Bugs.track(it) }
                        .doFinally { stater.update { it.copy(currentOperation = null) } }
                        .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                        .subscribe(
                                { closeDialogEvent.postValue(Any()) },
                                { errorEvent.postValue(it) }
                        )
                        .withScopeVDC(this)
            }
            DETACH -> {
                storageManager.detach(storageId, wipe = false)
                        .subscribeOn(Schedulers.io())
                        .doOnError { Bugs.track(it) }
                        .doFinally { stater.update { it.copy(currentOperation = null) } }
                        .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                        .subscribe(
                                { closeDialogEvent.postValue(Any()) },
                                { errorEvent.postValue(it) }
                        )
            }
            DELETE -> {
                storageManager.detach(storageId, wipe = true)
                        .subscribeOn(Schedulers.io())
                        .doOnError { Bugs.track(it) }
                        .doFinally { stater.update { it.copy(currentOperation = null) } }
                        .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                        .subscribe(
                                { closeDialogEvent.postValue(Any()) },
                                { errorEvent.postValue(it) }
                        )
            }
        }
    }

    fun cancelCurrentOperation() {
        stater.snapshot.currentOperation?.dispose()
    }

    data class State(
            val storageInfo: Storage.Info? = null,
            val allowedActions: List<Confirmable<StorageAction>> = listOf(),
            val isCancelable: Boolean = false,
            val isLoadingData: Boolean = false,
            val currentOperation: Disposable? = null
    ) {
        val isWorking: Boolean
            get() = currentOperation != null
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageActionDialogVDC
    }
}