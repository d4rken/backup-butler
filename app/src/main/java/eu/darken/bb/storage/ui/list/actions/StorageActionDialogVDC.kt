package eu.darken.bb.storage.ui.list.actions

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.takeUntilAfter
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.common.ui.Confirmable
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageBuilder
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.actions.StorageAction.*
import eu.darken.bb.storage.ui.viewer.StorageViewerOptions
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import javax.inject.Inject

@HiltViewModel
class StorageActionDialogVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val storageBuilder: StorageBuilder,
    private val taskBuilder: TaskBuilder,
    dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<StorageActionDialogArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val stater = DynamicStateFlow(TAG, vdcScope) { State(isLoadingData = true) }
    val state = stater.asLiveData2()
    val closeDialogEvent = SingleLiveEvent<Any>()

    init {
        storageManager.infos(listOf(storageId))
            .map { it.single() }
            .takeUntilAfter { info -> info.isFinished }
            .onEach { infoOpt ->
                val allowedActions = mutableSetOf<Confirmable<StorageAction>>().apply {
                    if (infoOpt.info?.status != null) {
                        add(Confirmable(VIEW) { storageAction(it) })
                        add(Confirmable(RESTORE) { storageAction(it) })
                    }

                    if (infoOpt.info?.config != null) {
                        add(Confirmable(EDIT) { storageAction(it) })
                    }
                    if (infoOpt.info?.status?.isReadOnly == false) {
                        add(Confirmable(DELETE, requiredLvl = 2) { storageAction(it) })
                    }

                    add(Confirmable(DETACH, requiredLvl = 1) { storageAction(it) })
                }

                stater.updateBlocking {
                    copy(
                        storageInfo = infoOpt.info,
                        allowedActions = allowedActions.toList(),
                        isLoadingData = !infoOpt.isFinished
                    )
                }

                if (infoOpt.anyError != null) {
                    errorEvents.postValue(infoOpt.anyError)
                }
            }
            .launchInViewModel()
    }

    fun storageAction(action: StorageAction) = launch {
        require(stater.value().job == null)
        stater.updateBlocking { copy(job = coroutineContext.job) }
        try {
            when (action) {
                VIEW -> {
                    StorageActionDialogDirections.actionStorageActionDialogToStorageViewer(
                        viewerOptions = StorageViewerOptions(storageId = storageId)
                    ).navVia(this@StorageActionDialogVDC)
                    closeDialogEvent.postValue(Any())
                }
                EDIT -> {
                    val load = storageBuilder.load(storageId)!!
                    StorageActionDialogDirections.actionStorageActionDialogToStorageEditor(
                        storageId = load.storageId
                    ).navVia(this@StorageActionDialogVDC)
                    closeDialogEvent.postValue(Any())
                }
                RESTORE -> {
                    val data = taskBuilder.getEditor(type = Task.Type.RESTORE_SIMPLE)
                    (data.editor as SimpleRestoreTaskEditor).addStorageId(storageId)

                    StorageActionDialogDirections.actionStorageActionDialogToTaskEditor(
                        args = TaskEditorArgs(
                            taskId = data.taskId,
                            taskType = Task.Type.RESTORE_SIMPLE,
                            isSingleUse = true
                        )
                    ).navVia(navEvents)
                    closeDialogEvent.postValue(Any())
                }
                DETACH -> {
                    val ref = storageManager.detach(storageId, wipe = false)
                    navEvents.postValue(null)
                }
                DELETE -> {
                    val ref = storageManager.detach(storageId, wipe = true)
                    closeDialogEvent.postValue(Any())
                }
            }

        } finally {
            stater.updateBlocking { copy(job = null) }
        }
    }

    fun cancelCurrentOperation() = launch {
        stater.value().job?.cancel()
    }

    data class State(
        val storageInfo: Storage.Info? = null,
        val allowedActions: List<Confirmable<StorageAction>> = listOf(),
        val isCancelable: Boolean = false,
        val isLoadingData: Boolean = false,
        val job: Job? = null
    ) {
        val isWorking: Boolean
            get() = job != null
    }

    companion object {
        private val TAG = logTag("Storage", "ActionDialog", "VDC")
    }
}