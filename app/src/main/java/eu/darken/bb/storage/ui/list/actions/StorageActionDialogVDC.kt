package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.Bugs
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.navigation.NavDirectionsProvider
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.via
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class StorageActionDialogVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val storageBuilder: StorageBuilder,
    private val taskBuilder: TaskBuilder
) : SmartVDC(), NavDirectionsProvider {

    private val navArgs by handle.navArgs<StorageActionDialogArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val stater = Stater { State(isLoadingData = true) }
    val state = stater.liveData
    override val navEvents = SingleLiveEvent<NavDirections>()
    val closeDialogEvent = SingleLiveEvent<Any>()
    val errorEvent = SingleLiveEvent<Throwable>()

    init {
        storageManager.infos(listOf(storageId))
            .observeOn(Schedulers.computation())
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
                        storageInfo = infoOpt.info,
                        allowedActions = allowedActions.toList(),
                        isLoadingData = !infoOpt.isFinished
                    )
                }

                if (infoOpt.anyError != null) {
                    errorEvent.postValue(infoOpt.anyError)
                }
            }
            .withScopeVDC(this)
    }

    fun storageAction(action: StorageAction) {
        require(stater.snapshot.currentOperation == null)

        when (action) {
            VIEW -> {
                Single
                    .fromCallable {
                        // do we need to do more checks here, or will that always be part of the viewer activity
                        storageId
                    }
                    .subscribeOn(Schedulers.computation())
                    .doFinally { stater.update { it.copy(currentOperation = null) } }
                    .doOnSubscribe { disp ->
                        stater.update { it.copy(currentOperation = disp) }
                    }
                    .doOnError { Bugs.track(it) }
                    .doFinally { stater.update { it.copy(currentOperation = null) } }
                    .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                    .subscribe({
                        StorageActionDialogDirections.actionStorageActionDialogToStorageViewerActivity(it)
                            .run { navEvents.postValue(this) }
                        closeDialogEvent.postValue(Any())
                    }, {
                        errorEvent.postValue(it)
                    })
                    .withScopeVDC(this)
            }
            EDIT -> {
                storageBuilder.load(storageId)
                    .observeOn(Schedulers.computation())
                    .doOnError { Bugs.track(it) }
                    .doFinally { stater.update { it.copy(currentOperation = null) } }
                    .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                    .subscribe(
                        {
                            StorageActionDialogDirections.actionStorageActionDialogToStorageEditor(
                                storageId = it.storageId
                            ).via(this)
                            closeDialogEvent.postValue(Any())
                        },
                        { errorEvent.postValue(it) }
                    )
                    .withScopeVDC(this)
            }
            RESTORE -> {
                taskBuilder.getEditor(type = Task.Type.RESTORE_SIMPLE)
                    .observeOn(Schedulers.computation())
                    .flatMap { data ->
                        (data.editor as SimpleRestoreTaskEditor).addStorageId(storageId).map { data.taskId }
                    }
                    .doOnError { Bugs.track(it) }
                    .doFinally { stater.update { it.copy(currentOperation = null) } }
                    .doOnSubscribe { disp -> stater.update { it.copy(currentOperation = disp) } }
                    .subscribe({
                        StorageActionDialogDirections.actionStorageActionDialogToTaskEditor(
                            args = TaskEditorArgs(taskId = it, taskType = Task.Type.RESTORE_SIMPLE)
                        ).via(navEvents)
                        closeDialogEvent.postValue(Any())
                    }, {
                        errorEvent.postValue(it)
                    })
                    .withScopeVDC(this)
            }
            DETACH -> {
                storageManager.detach(storageId, wipe = false)
                    .observeOn(Schedulers.computation())
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
                    .observeOn(Schedulers.computation())
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
}