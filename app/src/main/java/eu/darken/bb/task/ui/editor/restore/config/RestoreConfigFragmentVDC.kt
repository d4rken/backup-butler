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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.awaitFirst

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
    private val customConfigs = editorObs.flatMap { it.customConfigs }

    private val summaryStater = Stater(SummaryState())
    val summaryState = summaryStater.liveData

    private val configStater = Stater(ConfigState())
    val configState = configStater.liveData

    init {
        dataObs
                .subscribe { data ->
                    val customCount = data.customConfigs
                            .filterNot { data.defaultConfigs.values.contains(it.value) }
                            .size
                    summaryStater.update { state ->
                        state.copy(
                                backupTypes = data.defaultConfigs.values.map { it.restoreType },
                                customConfigCount = customCount,
                                workIds = state.clearWorkId()
                        )
                    }
                    configStater.update { state ->
                        state.copy(
                                defaultConfigs = data.defaultConfigs.values.sortedBy { it.restoreType }.toList()
                        )
                    }
                }
                .withScopeVDC(this)

        customConfigs
                .subscribe { customConfigs ->
                    configStater.update { state ->
                        state.copy(
                                customConfigs = customConfigs.sortedBy {
                                    it.backupInfo.backupId.idString
                                }.toList(),
                                workIds = state.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateConfig(config: Restore.Config, target: Backup.Id? = null) {
        GlobalScope.launch {
            //            configStater.updateBlocking {
//                it.copy(workIds = it.addWorkId(WorkId.DEFAULT))
//            }
            val editor = editorObs.awaitFirst()
            if (target == null) {
                editor.updateDefaultConfig(config).blockingGet()
            } else {
                editor.updateCustomConfig(target, config).blockingGet()
            }
        }
    }

    data class SummaryState(
            val backupTypes: List<Backup.Type> = emptyList(),
            val customConfigCount: Int = 0,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    data class ConfigState(
            val defaultConfigs: List<Restore.Config> = emptyList(),
            val customConfigs: List<SimpleRestoreTaskEditor.CustomConfigWrap> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreConfigFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreConfigFragmentVDC
    }
}