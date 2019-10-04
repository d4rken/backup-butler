package eu.darken.bb.task.ui.editor.restore.sources

import androidx.lifecycle.SavedStateHandle
import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.ui.list.StorageInfoOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.schedulers.Schedulers

class RestoreSourcesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val storageManager: StorageManager
) : SmartVDC() {
    private val editorObs = taskBuilder.task(taskId)
            .subscribeOn(Schedulers.io())
            .filter { it.editor != null }
            .map { it.editor as SimpleRestoreTaskEditor }
            .replayingShare()

    private val configObs = editorObs.flatMap { it.config }
            .replayingShare()

    private val editor: SimpleRestoreTaskEditor by lazy { editorObs.blockingFirst() }

    private val countStater = Stater(CountState())
    val countState = countStater.liveData

    private val backupsStater = Stater(BackupsState())
    val backupsState = backupsStater.liveData

    init {
        configObs
                .subscribe { config ->
                    countStater.update { oldState ->
                        oldState.copy(
                                sourceStorages = config.targetStorage.toList(),
                                sourceBackupSpecs = config.targetBackupSpec.toList(),
                                sourceBackups = config.targetBackup.toList(),
                                workIds = oldState.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)

        configObs.map { it.targetStorage }
                .flatMap { storageManager.infos(it) }
                .subscribe { storageInfos ->
                    backupsStater.update { oldState ->
                        oldState.copy(
                                storages = storageInfos.toList(),
                                workIds = oldState.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)

//        configObs
//                .subscribe { config ->
//                    backupsStater.update { oldState ->
//                        oldState.copy(
//                                storages = config.targetStorage.toList(),
//                                specs = config.targetBackupSpec.toList(),
//                                backups = config.targetBackup.toList(),
//                                workIds = oldState.clearWorkId()
//                        )
//                    }
//                }
//                .withScopeVDC(this)
    }

    data class CountState(
            val sourceStorages: List<Storage.Id> = emptyList(),
            val sourceBackupSpecs: List<BackupSpec.Target> = emptyList(),
            val sourceBackups: List<Backup.Target> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State


    data class BackupsState(
            val storages: List<StorageInfoOpt> = emptyList(),
            val specs: List<BackupSpec.Target> = emptyList(),
            val backups: List<Backup.Target> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreSourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreSourcesFragmentVDC
    }
}