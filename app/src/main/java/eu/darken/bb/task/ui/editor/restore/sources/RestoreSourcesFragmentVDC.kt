package eu.darken.bb.task.ui.editor.restore.sources

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RestoreSourcesFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<RestoreSourcesFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorFlow = taskBuilder.task(taskId)
        .filter { it.editor != null }
        .map { it.editor as SimpleRestoreTaskEditor }
        .replayingShare(vdcScope)

    private val editorData = editorFlow
        .flatMapConcat { it.editorData }
        .replayingShare(vdcScope)

    private suspend fun getEditor(): SimpleRestoreTaskEditor = editorFlow.first()

    private val summaryStater = DynamicStateFlow(TAG, vdcScope) { CountState() }
    val summaryState = summaryStater.asLiveData2()

    private val backupsStater = DynamicStateFlow(TAG, vdcScope) { BackupsState() }
    val backupsState = backupsStater.asLiveData2()

    val finishEvent = SingleLiveEvent<Any>()

    init {
        editorData
            .onEach { data ->
                if (data.backupTargets.isEmpty()) {
                    finishEvent.postValue(Any())
                } else {
                    summaryStater.updateBlocking {
                        copy(
                            sourceBackups = data.backupTargets.toList(),
                            workIds = this.clearWorkId()
                        )
                    }
                }
            }
            .launchInViewModel()

        flow { emit(getEditor()) }
            .flatMapConcat { it.backupInfos }
            .onEach { backupInfos ->
                backupsStater.updateBlocking {
                    copy(
                        backups = backupInfos.toList(),
                        workIds = this.clearWorkId()
                    )
                }
            }
            .launchInViewModel()
    }

    fun exclude(infoOpt: Backup.InfoOpt) = launch {
        Timber.tag(TAG).i("Excluding %s", infoOpt)
        getEditor().excludeBackup(infoOpt.backupId)
    }

    fun continueWithSources() {
        RestoreSourcesFragmentDirections.actionRestoreSourcesFragmentToRestoreConfigFragment(
            taskId = navArgs.taskId
        ).navVia(this)
    }

    data class CountState(
        val sourceBackups: List<Backup.Target> = emptyList(),
        override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    data class BackupsState(
        val backups: List<Backup.InfoOpt> = emptyList(),
        override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    companion object {
        val TAG = logTag("Task", "Restore", "Simple", "Sources", "VDC")
    }
}