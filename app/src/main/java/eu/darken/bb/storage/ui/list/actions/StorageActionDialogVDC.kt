package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
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

    private val stateUpdater = Stater(State(loading = true))
    val state = stateUpdater.liveData

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
                                }
                                allowedActions.add(DELETE)
                            }
                            stateUpdater.update {
                                if (storageInfo == null) {
                                    it.copy(loading = true, finished = true)
                                } else {
                                    it.copy(
                                            storageInfo = storageInfo,
                                            loading = false,
                                            allowedActions = allowedActions.toList()
                                    )
                                }
                            }
                        },
                        {
                            stateUpdater.update { it.copy(finished = true) }
                        }
                )
    }

    fun storageAction(action: StorageAction) {
        when (action) {
            VIEW -> {
                storageManager.startViewer(storageId)
                stateUpdater.update { it.copy(loading = false, finished = true) }
            }
            EDIT -> {
                storageBuilder.load(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                        .subscribe { storage ->
                            storageBuilder.startEditor(storage.storageId)
                        }
            }
            DELETE -> {
                storageRefRepo.remove(storageId)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                        .subscribe()
            }
            RESTORE -> {
                taskBuilder.createBuilder(type = Task.Type.RESTORE_SIMPLE)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { stateUpdater.update { it.copy(loading = true) } }
                        .map {
                            (it.editor as SimpleRestoreTaskEditor).addStorageId(storageId)
                            it
                        }
                        .flatMap { data -> taskBuilder.update(data.taskId) { data }.map { it.notNullValue() } }
                        .delay(200, TimeUnit.MILLISECONDS)
                        .doFinally { stateUpdater.update { it.copy(loading = false, finished = true) } }
                        .subscribe { data ->
                            taskBuilder.startEditor(data.taskId)
                        }
            }
        }
    }

    data class State(
            val loading: Boolean = false,
            val finished: Boolean = false,
            val storageInfo: StorageInfo? = null,
            val allowedActions: List<StorageAction> = listOf()
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id): StorageActionDialogVDC
    }
}