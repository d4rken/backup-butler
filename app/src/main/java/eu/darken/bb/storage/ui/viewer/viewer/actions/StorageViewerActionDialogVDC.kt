package eu.darken.bb.storage.ui.viewer.viewer.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.R
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.Operation
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.onError
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.common.toCaString
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.job
import javax.inject.Inject

@HiltViewModel
class StorageViewerActionDialogVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    storageManager: StorageManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<StorageViewerActionDialogArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val backupSpecId: BackupSpec.Id = navArgs.specId

    private val storageFlow = flow { emit(storageManager.getStorage(storageId)) }

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    val actionEvent = SingleLiveEvent<Triple<StorageViewerAction, Storage.Id, BackupSpec.Id>>()

    init {
        storageFlow
            .flatMapConcat { it.specInfos() }
            .map { contents -> contents.single { it.backupSpec.specId == backupSpecId } }
            .take(1)
            .onEach { content ->
                stater.updateBlocking { copy(info = content) }
            }
            .onError { navEvents.postValue(null) }
            .launchInViewModel()

        storageFlow
            .flatMapConcat { it.info() }
            .filter { it.isFinished }
            .take(1)
            .onEach { info ->
                val actions = mutableListOf<Confirmable<StorageViewerAction>>().apply {
                    add(Confirmable(StorageViewerAction.VIEW) { storageAction(it) })
                    add(Confirmable(StorageViewerAction.RESTORE) { storageAction(it) })
                    if (info.status?.isReadOnly == false) {
                        add(Confirmable(StorageViewerAction.DELETE, requiredLvl = 1) { storageAction(it) })
                    }
                }.toList()
                stater.updateBlocking { copy(allowedActions = actions) }
            }
            .onError { navEvents.postValue(null) }
            .launchInViewModel()
    }

    fun storageAction(action: StorageViewerAction) = launch {
        require(stater.value().currentOp == null)

        try {
            when (action) {
                StorageViewerAction.VIEW -> {
                    actionEvent.postValue(Triple(StorageViewerAction.VIEW, storageId, backupSpecId))
                }
                StorageViewerAction.DELETE -> {
                    stater.updateBlocking {
                        copy(
                            currentOp = Operation(
                                job = coroutineContext.job,
                                label = R.string.progress_deleting_label.toCaString()
                            )
                        )
                    }

                    storageFlow.first().remove(backupSpecId)
                    navEvents.postValue(null)

                }
                StorageViewerAction.RESTORE -> {
                    stater.updateBlocking {
                        copy(
                            currentOp = Operation(
                                job = coroutineContext.job,
                                label = R.string.progress_loading_label.toCaString()
                            )
                        )
                    }
                    val data = taskBuilder.getEditor(type = Task.Type.RESTORE_SIMPLE)
                    (data.editor as SimpleRestoreTaskEditor).addBackupSpecId(storageId, backupSpecId)

                    StorageViewerActionDialogDirections.actionStorageItemActionDialogToTaskEditor(
                        args = TaskEditorArgs(
                            taskId = data.taskId,
                            taskType = Task.Type.RESTORE_SIMPLE,
                            isSingleUse = true
                        )
                    ).run { navEvents.postValue(this) }
                }
            }
        } finally {
            stater.updateBlocking { copy(currentOp = null) }
        }
    }

    data class State(
        val info: BackupSpec.Info? = null,
        val allowedActions: List<Confirmable<StorageViewerAction>>? = null,
        val currentOp: Operation? = null
    ) {
        val isWorking: Boolean
            get() = currentOp != null || allowedActions == null
    }

    companion object {
        private val TAG = logTag("Storage", "Item", "ActionDialog", "VDC")
    }
}