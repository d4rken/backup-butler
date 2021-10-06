package eu.darken.bb.storage.ui.viewer.content.page

import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx3.replayingShare
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ContentPageFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val storageManager: StorageManager,
    private val taskBuilder: TaskBuilder
) : SmartVDC() {
    private val navArgs by handle.navArgs<ContentPageFragmentArgs>()
    private val storageId: Storage.Id = navArgs.storageId
    private val backupSpecId: BackupSpec.Id = navArgs.specId
    private val backupId: Backup.Id = navArgs.backupId

    private val storageObs = storageManager.getStorage(storageId).observeOn(Schedulers.computation())
    private val contentObs = storageObs.flatMapObservable { it.specInfos() }
        .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
        .replayingShare()

    private val stater: Stater<State> = Stater(tag = TAG) { State() }
    val state = stater.liveData

    val finishEvent = SingleLiveEvent<Any>()

    init {
        Timber.tag(TAG).v("StorageId %s, BackupSpecId: %s, BackupId: %s", storageId, backupSpecId, backupId)
        contentObs
            .subscribe({ content ->
                val version = content.backups.find { it.backupId == backupId }!!
                stater.update {
                    it.copy(
                        specInfo = content,
                        metaData = version,
                        isLoadingInfos = false,
                        showRestoreAction = true
                    )
                }
            }, {
                finishEvent.postValue(Any())
            })
            .withScopeVDC(this)

        contentObs
            .flatMap { content -> storageObs.flatMapObservable { it.backupContent(content.specId, backupId) } }
            .subscribe({ details ->
                stater.update { state ->
                    state.copy(
                        items = details.items.toList(),
                        isLoadingItems = false
                    )
                }
            }, { err ->
                stater.update { it.copy(error = err) }
            })
            .withScopeVDC(this)
    }


    fun restore() {
        taskBuilder.createEditor(type = Task.Type.RESTORE_SIMPLE)
            .observeOn(Schedulers.computation())
            .doOnSubscribe { stater.update { it.copy(showRestoreAction = false) } }
            .flatMap { data ->
                val type = contentObs.blockingFirst().backupSpec.backupType
                (data.editor as SimpleRestoreTaskEditor).addBackupId(storageId, backupSpecId, backupId, type)
                    .map { data.taskId }
            }
            .flatMapCompletable { taskBuilder.startEditor(it) }
            .doFinally { finishEvent.postValue(Any()) }
            .subscribe()
            .withScopeVDC(this)
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