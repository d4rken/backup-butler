package eu.darken.bb.task.ui.editor.restore.sources

import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx3.replayingShare
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RestoreSourcesFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    @Assisted private val taskId: Task.Id,
    private val taskBuilder: TaskBuilder
) : SmartVDC() {
    private val editorObs = taskBuilder.task(taskId)
        .subscribeOn(Schedulers.io())
        .filter { it.editor != null }
        .map { it.editor as SimpleRestoreTaskEditor }
        .replayingShare()

    private val editorData = editorObs.flatMap { it.editorData }
        .replayingShare()

    private val editor: SimpleRestoreTaskEditor by lazy { editorObs.blockingFirst() }

    private val summaryStater = Stater(CountState())
    val summaryState = summaryStater.liveData

    private val backupsStater = Stater(BackupsState())
    val backupsState = backupsStater.liveData

    val finishEvent = SingleLiveEvent<Any>()

    init {
        editorData
            .subscribe { data ->
                if (data.backupTargets.isEmpty()) {
                    finishEvent.postValue(Any())
                } else {
                    summaryStater.update { oldState ->
                        oldState.copy(
                            sourceBackups = data.backupTargets.toList(),
                            workIds = oldState.clearWorkId()
                        )
                    }
                }
            }
            .withScopeVDC(this)

        editor.backupInfos
            .subscribe { backupInfos ->
                backupsStater.update { oldState ->
                    oldState.copy(
                        backups = backupInfos.toList(),
                        workIds = oldState.clearWorkId()
                    )
                }
            }
            .withScopeVDC(this)
    }

    fun exclude(infoOpt: Backup.InfoOpt) {
        Timber.tag(TAG).i("Excluding %s", infoOpt)
        editor.excludeBackup(infoOpt.backupId)
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
        val TAG = App.logTag("Task", "Restore", "Simple", "Sources", "VDC")
    }
}