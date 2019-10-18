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
import eu.darken.bb.storage.core.backupInfosOpt
import eu.darken.bb.storage.core.specInfosOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.Observable
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

    private val summaryStater = Stater(CountState())
    val countState = summaryStater.liveData

    private val backupsStater = Stater(BackupsState())
    val backupsState = backupsStater.liveData

    init {
        configObs
                .subscribe { config ->
                    summaryStater.update { oldState ->
                        oldState.copy(
                                sourceStorages = config.targetStorages.toList(),
                                sourceBackupSpecs = config.targetBackupSpec.toList(),
                                sourceBackups = config.targetBackup.toList(),
                                workIds = oldState.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)

        configObs.map { it.targetStorages }
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

        configObs.map { it.targetBackupSpec }
                .switchMap { targets ->
                    val obs = targets.map { target ->
                        storageManager.getStorage(target.storageId)
                                .subscribeOn(Schedulers.io())
                                .switchMap { it.specInfosOpt(target.backupSpecId) }
                                .map { it.first() }
                    }
                    return@switchMap Observable.combineLatest(obs) { it.asList() as List<BackupSpec.InfoOpt> }
                }
                .subscribe { specInfos ->
                    backupsStater.update { oldState ->
                        oldState.copy(
                                specs = specInfos.toList(),
                                workIds = oldState.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)

        configObs.map { it.targetBackup }
                .switchMap { targets ->
                    val obs = targets.map { target ->
                        storageManager.getStorage(target.storageId)
                                .subscribeOn(Schedulers.io())
                                .switchMap { it.backupInfosOpt(Pair(target.backupSpecId, target.backupId)) }
                                .map { it.first() }
                    }
                    return@switchMap Observable.combineLatest(obs) { it.asList() as List<Backup.InfoOpt> }
                }
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

    data class CountState(
            val sourceStorages: List<Storage.Id> = emptyList(),
            val sourceBackupSpecs: List<BackupSpec.Target> = emptyList(),
            val sourceBackups: List<Backup.Target> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State


    data class BackupsState(
            val storages: List<Storage.InfoOpt> = emptyList(),
            val specs: List<BackupSpec.InfoOpt> = emptyList(),
            val backups: List<Backup.InfoOpt> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreSourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreSourcesFragmentVDC
    }
}