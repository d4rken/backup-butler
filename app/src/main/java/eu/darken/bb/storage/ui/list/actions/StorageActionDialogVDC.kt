package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.*
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentVDC
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class StorageActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder,
        private val storageRefRepo: StorageRefRepo,
        private val taskBuilder: TaskBuilder
) : SmartVDC() {

    private val stateUpdater = Stater(State(isWorking = true))
    val state = stateUpdater.liveData
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageManager.info(storageId)
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .subscribe(
                        { storageInfo ->
                            val allowedActions = mutableSetOf<StorageAction>()
                            if (storageInfo != null) {
                                if (storageInfo.config != null) {
                                    allowedActions.add(VIEW)
                                    allowedActions.add(RESTORE)
                                    allowedActions.add(EDIT)
                                    if (storageInfo.status?.isReadOnly == false) allowedActions.add(DELETE)
                                }
                                allowedActions.add(DETACH)
                            }
                            stateUpdater.update {
                                if (storageInfo == null) {
                                    finishedEvent.postValue(Any())
                                    it.copy(isWorking = true)
                                } else {
                                    it.copy(
                                            storageInfo = storageInfo,
                                            isWorking = false,
                                            allowedActions = allowedActions.toList()
                                    )
                                }
                            }
                        },
                        {
                            finishedEvent.postValue(Any())
                        }
                )
    }

    fun storageAction(action: StorageAction) {
        when (action) {
            VIEW -> {
                storageManager.startViewer(storageId)
                finishedEvent.postValue(Any())
            }
            EDIT -> {
                storageBuilder.load(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { finishedEvent.postValue(Any()) }
                        .flatMapCompletable { storageBuilder.startEditor(it.storageId) }
                        .subscribe()
            }
            RESTORE -> {
                taskBuilder.createBuilder(type = Task.Type.RESTORE_SIMPLE)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                        .map {
                            (it.editor as SimpleRestoreTaskEditor).addStorageId(storageId)
                            it
                        }
                        .flatMap { data -> taskBuilder.update(data.taskId) { data }.map { it.notNullValue() } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { finishedEvent.postValue(Any()) }
                        .flatMapCompletable { taskBuilder.startEditor(it.taskId) }
                        .subscribe()
            }
            DETACH -> {
                storageManager.detach(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { finishedEvent.postValue(Any()) }
                        .subscribe()
            }
            DELETE -> TODO()
        }
    }

    data class State(
            val storageInfo: StorageInfo? = null,
            val allowedActions: List<StorageAction> = listOf(),
            val isWorking: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageActionDialogVDC
    }
}