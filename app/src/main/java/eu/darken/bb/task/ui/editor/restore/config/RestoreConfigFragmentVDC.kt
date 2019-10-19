package eu.darken.bb.task.ui.editor.restore.config

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.schedulers.Schedulers

class RestoreConfigFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder
) : SmartVDC() {
    private val editorObs = taskBuilder.task(taskId)
            .subscribeOn(Schedulers.io())
            .filter { it.editor != null }
            .map { it.editor as SimpleRestoreTaskEditor }

    private val dataObs = editorObs.flatMap { it.editorData }

    private val summaryStater = Stater(SummaryState())
    val summaryState = summaryStater.liveData

    private val configStater = Stater(ConfigState())
    val configState = configStater.liveData

    init {
        dataObs
                .map { it.defaultConfigs }
                .subscribe { configs ->
                    summaryStater.update { state ->
                        state.copy(
                                backupTypes = configs.values.map { it.restoreType },
                                workIds = state.clearWorkId()
                        )
                    }
                    configStater.update { state ->
                        state.copy(
                                restoreConfigs = configs.values.toList(),
                                workIds = state.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateConfig(config: Restore.Config) {
//        GlobalScope.launch {
//            stater.updateBlocking { it.copy(isLoading = true) }
//            editorObs.awaitFirst().updateConfig(config)
//        }
    }

    data class SummaryState(
            val backupTypes: List<Backup.Type> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    data class ConfigState(
            val restoreConfigs: List<Restore.Config> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreConfigFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreConfigFragmentVDC
    }
}