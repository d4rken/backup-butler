package eu.darken.bb.storage.ui.viewer.content.page

import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.Versioning
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ContentPageFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val storageId: Storage.Id,
        @Assisted private val backupSpecId: BackupSpec.Id,
        @Assisted private val backupId: Backup.Id,
        private val storageManager: StorageManager,
        private val taskBuilder: TaskBuilder
) : SmartVDC() {

    private val storageObs = storageManager.getStorage(storageId)
            .subscribeOn(Schedulers.io())
            .replayingShare()
    private val contentObs = storageObs.switchMap { it.items() }
            .map { contents -> contents.find { it.backupSpec.specId == backupSpecId }!! }
            .replayingShare()

    private val stater: Stater<State> = Stater(State())
    val state = stater.liveData

    val finishEvent = SingleLiveEvent<Any>()

    init {
        Timber.tag(TAG).v("StorageId %s, BackupSpecId: %s, BackupId: %s", storageId, backupSpecId, backupId)
        contentObs
                .subscribe({ content ->
                    val version = content.versioning.versions.find { it.backupId == backupId }!!
                    stater.update {
                        it.copy(
                                specInfo = content,
                                version = version,
                                isLoadingInfos = false,
                                showRestoreAction = true
                        )
                    }
                }, {
                    finishEvent.postValue(Any())
                })
                .withScopeVDC(this)

        contentObs
                .flatMap { content -> storageObs.switchMap { it.content(content.specId, backupId) } }
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
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stater.update { it.copy(showRestoreAction = false) } }
                .flatMap { data ->
                    (data.editor as SimpleRestoreTaskEditor).addBackupId(storageId, backupSpecId, backupId)
                    taskBuilder.update(data.taskId) { data }.map { data }
                }
                .flatMapCompletable { taskBuilder.startEditor(it.taskId, Task.Type.RESTORE_SIMPLE) }
                .doFinally {
                    finishEvent.postValue(Any())
                }
                .subscribe()
                .withScopeVDC(this)
    }

    data class State(
            val specInfo: BackupSpec.Info? = null,
            val version: Versioning.Version? = null,
            val items: List<Backup.Content.Entry> = emptyList(),
            val isLoadingInfos: Boolean = true,
            val isLoadingItems: Boolean = true,
            val showRestoreAction: Boolean = false,
            val error: Throwable? = null
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<ContentPageFragmentVDC> {
        fun create(handle: SavedStateHandle, storageId: Storage.Id, backupSpecId: BackupSpec.Id, backupId: Backup.Id): ContentPageFragmentVDC
    }

    companion object {
        val TAG = App.logTag("Storage", "Details", "Page", "VDC")
    }
}