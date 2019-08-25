package eu.darken.bb.task.ui.editor.restore.config

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.Stater
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.storage.core.StorageInfo
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.awaitFirst
import timber.log.Timber

class RestoreConfigFragmentVDC @AssistedInject constructor(
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
            .doOnSubscribe { Timber.i("SUB1") }
            .doFinally { Timber.i("DISP1") }

    private val stater = Stater(State())
    val state = stater.liveData

    init {
        configObs
                .subscribe { task ->
                    stater.update {
                        it.copy(
                                isLoading = false,
                                restoreConfigs = task.restoreConfigs.toList()
                        )
                    }
                }
                .withScopeVDC(this)

        configObs.map { it.storageIds }
                .flatMapIterable { it }
                .flatMap { storageManager.info(it) }
                .doOnSubscribe { Timber.i("SUB2") }
                .doFinally { Timber.i("DISP2") }
                .subscribe { info ->
                    stater.update { oldState ->
                        val newStorageInfos = oldState.sourceStorages.filterNot {
                            it.ref.storageId == info.ref.storageId
                        }.toMutableList()
                        newStorageInfos.add(info)
                        oldState.copy(sourceStorages = newStorageInfos.toList())
                    }
                }
                .withScopeVDC(this)
    }

    fun updateConfig(config: Restore.Config) {
        GlobalScope.launch {
            stater.updateBlocking { it.copy(isLoading = true) }
            editorObs.awaitFirst().updateConfig(config)
        }
    }

    data class State(
            val sourceStorages: List<StorageInfo> = emptyList(),
            val sourceBackupSpecs: List<StorageInfo> = emptyList(),
            val sourceBackups: List<StorageInfo> = emptyList(),
            val restoreConfigs: List<Restore.Config> = emptyList(),
            val isLoading: Boolean = true
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreConfigFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreConfigFragmentVDC
    }
}