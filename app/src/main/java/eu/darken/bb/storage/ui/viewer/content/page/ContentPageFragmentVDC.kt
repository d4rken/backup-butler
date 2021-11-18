package eu.darken.bb.storage.ui.viewer.content.page

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.onError
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.viewer.content.ItemContentsFragmentDirections
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import eu.darken.bb.task.ui.editor.TaskEditorArgs
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ContentPageFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val taskBuilder: TaskBuilder,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<ContentPageFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val backupSpecId: BackupSpec.Id = navArgs.specId
    private val backupId: Backup.Id = navArgs.backupId

    private val storageFlow = flow { emit(storageManager.getStorage(storageId)) }

    private val contentFlow = storageFlow.flatMapConcat { it.specInfos() }
        .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
        .replayingShare(vdcScope)

    private val stater = DynamicStateFlow(TAG, vdcScope) { State() }
    val state = stater.asLiveData2()

    init {
        Timber.tag(TAG).v("StorageId %s, BackupSpecId: %s, BackupId: %s", storageId, backupSpecId, backupId)
        contentFlow
            .onEach { content ->
                val version = content.backups.find { it.backupId == backupId }!!
                stater.updateBlocking {
                    copy(
                        specInfo = content,
                        metaData = version,
                        isLoadingInfos = false,
                        showRestoreAction = true
                    )
                }
            }
            .onCompletion { navEvents.postValue(null) }
            .launchInViewModel()

        contentFlow
            .flatMapConcat { content -> storageFlow.flatMapConcat { it.backupContent(content.specId, backupId) } }
            .onEach { details ->
                stater.updateBlocking {
                    copy(
                        items = details.items.toList(),
                        isLoadingItems = false
                    )
                }
            }
            .onError {
                stater.updateBlocking { copy(error = it) }
            }
            .launchInViewModel()
    }


    fun restore() = launch {
        stater.updateBlocking { copy(showRestoreAction = false) }

        val data = taskBuilder.getEditor(type = Task.Type.RESTORE_SIMPLE)

        val type = contentFlow.first().backupSpec.backupType
        (data.editor as SimpleRestoreTaskEditor).addBackupId(storageId, backupSpecId, backupId, type)

        ItemContentsFragmentDirections.actionItemContentsFragmentToTaskEditor(
            args = TaskEditorArgs(taskId = data.taskId, taskType = Task.Type.RESTORE_SIMPLE)
        ).navVia(this@ContentPageFragmentVDC)

        navEvents.postValue(null)
    }

    data class State(
        val specInfo: BackupSpec.Info? = null,
        val metaData: Backup.MetaData? = null,
        val items: List<Backup.ContentInfo.Entry> = emptyList(),
        val isLoadingInfos: Boolean = true,
        val isLoadingItems: Boolean = true,
        val showRestoreAction: Boolean = false,
        val error: Throwable? = null
    )

    companion object {
        val TAG = logTag("Storage", "Details", "Page", "VDC")
    }
}