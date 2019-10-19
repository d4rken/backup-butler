package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
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
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class StorageActionDialogVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        private val storageManager: StorageManager,
        private val storageBuilder: StorageBuilder,
        private val taskBuilder: TaskBuilder
) : SmartVDC() {

    private val stateUpdater = Stater(State(isLoadingData = true))
    val state = stateUpdater.liveData
    val finishedEvent = SingleLiveEvent<Any>()

    init {
        storageManager.info(storageId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { storageInfo ->
                            if (storageInfo == null) {
                                finishedEvent.postValue(Any())
                                return@subscribe
                            }

                            val allowedActions = mutableSetOf<StorageAction>()
                            if (storageInfo.config != null) {
                                allowedActions.add(VIEW)
                                allowedActions.add(RESTORE)
                                allowedActions.add(EDIT)
                            }
                            allowedActions.add(DETACH)
                            if (storageInfo.status?.isReadOnly == false) allowedActions.add(DELETE)

                            stateUpdater.update {
                                it.copy(
                                        storageInfo = storageInfo,
                                        allowedActions = allowedActions.toList(),
                                        isLoadingData = storageInfo.status == null
                                )
                            }
                        },
                        {
                            finishedEvent.postValue(Any())
                        }
                )
                .withScopeVDC(this)
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
                taskBuilder.createEditor(type = Task.Type.RESTORE_SIMPLE)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                        .flatMap { data ->
                            (data.editor as SimpleRestoreTaskEditor).addStorageId(storageId).map { data.taskId }
                        }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .flatMapCompletable { taskBuilder.startEditor(it) }
                        .doFinally { finishedEvent.postValue(Any()) }
                        .subscribe()
            }
            DETACH -> {
                detachSub = storageManager.detach(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { finishedEvent.postValue(Any()) }
                        .subscribe()
            }
            DELETE -> {
                deletionSub = storageManager.wipe(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(isWorking = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doOnSuccess { finishedEvent.postValue(Any()) }
                        .subscribe()
            }
        }
    }

    private var deletionSub = Disposables.disposed()
    private var detachSub = Disposables.disposed()

    override fun onCleared() {
        detachSub.dispose()
        deletionSub.dispose()
        super.onCleared()
    }

    data class State(
            val storageInfo: Storage.Info? = null,
            val allowedActions: List<StorageAction> = listOf(),
            val isWorking: Boolean = false,
            val isLoadingData: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageActionDialogVDC
    }
}