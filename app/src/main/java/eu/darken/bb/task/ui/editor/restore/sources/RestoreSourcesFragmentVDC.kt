package eu.darken.bb.task.ui.editor.restore.sources

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
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

    private val configObs = editorObs.flatMap { it.config }
            .doOnNext { current ->
                stater.update {
                    it.copy(

                    )
                }
            }

    private val storageObs: Observable<StorageInfo> = configObs.map { it.storageIds }
            .flatMapIterable { it }
            .flatMap { storageManager.info(it) }
            .doOnNext { info ->
                stater.update { oldState ->
                    val newStorageInfos = oldState.sourceStorages.filterNot {
                        it.ref.storageId == info.ref.storageId
                    }.toMutableList()
                    newStorageInfos.add(info)
                    oldState.copy(sourceStorages = newStorageInfos.toList())
                }
            }

    private val editor: SimpleRestoreTaskEditor by lazy {
        editorObs.blockingFirst()
    }

    private val stater = Stater(State())
            .addLiveDep {
                storageObs.subscribe()
            }

    val state = stater.liveData

    fun updateConfig(config: Restore.Config) {
        stater.update { it.copy(isLoading = true) }
        editor.updateConfig(config)
    }

    data class State(
            val sourceStorages: List<StorageInfo> = emptyList(),
            val sourceBackupSpecs: List<StorageInfo> = emptyList(),
            val sourceBackups: List<StorageInfo> = emptyList(),
            val restoreConfigs: List<Restore.Config> = emptyList(),
            val isLoading: Boolean = true
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreSourcesFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreSourcesFragmentVDC
    }
}