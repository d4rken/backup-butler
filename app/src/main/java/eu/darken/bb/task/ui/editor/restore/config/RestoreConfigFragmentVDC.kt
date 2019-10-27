package eu.darken.bb.task.ui.editor.restore.config

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.WorkId
import eu.darken.bb.common.clearWorkId
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.picker.APathPicker
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
import timber.log.Timber

class RestoreConfigFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder,
        private val safGateway: SAFGateway
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

    val openPickerEvent = SingleLiveEvent<APathPicker.Options>()

    init {
        dataObs
                .subscribe { data ->
                    val customCount = data.customConfigs
                            .filterNot { data.defaultConfigs.values.contains(it.value) }
                            .size

                    val wrappedDefaults = data.defaultConfigs.values
                            .sortedBy { it.restoreType }
                            .map {
                                when (it.restoreType) {
                                    Backup.Type.FILES -> SimpleRestoreTaskEditor.FilesConfigWrap(it as FilesRestoreConfig)
                                    Backup.Type.APP -> SimpleRestoreTaskEditor.AppsConfigWrap(it as AppRestoreConfig)
                                }
                            }
                            .toList()
                    summaryStater.update { state ->
                        state.copy(
                                backupTypes = data.defaultConfigs.values.map { it.restoreType },
                                customConfigCount = customCount,
                                workIds = state.clearWorkId()
                        )
                    }
                    configStater.update { state ->
                        state.copy(
                                defaultConfigs = wrappedDefaults
                        )
                    }
                }
                .withScopeVDC(this)

        customConfigs
                .subscribe { customConfigs ->
                    configStater.update { state ->
                        state.copy(
                                customConfigs = customConfigs.sortedBy {
                                    it.backupInfoOpt!!.backupId.idString
                                }.toList(),
                                workIds = state.clearWorkId()
                        )
                    }
                }
                .withScopeVDC(this)
    }

    fun updateConfig(config: Restore.Config, target: Backup.Id? = null) {
        GlobalScope.launch {
            val editor = editorObs.awaitFirst()
            if (target == null) {
                editor.updateDefaultConfig(config).blockingGet()
            } else {
                editor.updateCustomConfig(target) { config }.blockingGet()
            }
        }
    }

    fun pathAction(configWrapper: SimpleRestoreTaskEditor.FilesConfigWrap, target: Backup.Id) {
        Timber.tag(TAG).d("updatePath(config=%s, target=%s)", configWrapper, target)
        openPickerEvent.postValue(APathPicker.Options())
    }

    fun updatePath(result: APathPicker.Result) {
        Timber.tag(TAG).d("updatePath(result=%s)", result)
        requireNotNull(result.path)
        val backupId: Backup.Id = result.payload.getParcelable("backupId")!!
        editorObs.firstOrError()
                .flatMap { it.updatePath(backupId, result.path) }
                .subscribe()
    }

    data class SummaryState(
            val backupTypes: List<Backup.Type> = emptyList(),
            val customConfigCount: Int = 0,
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    data class ConfigState(
            val defaultConfigs: List<SimpleRestoreTaskEditor.ConfigWrap> = emptyList(),
            val customConfigs: List<SimpleRestoreTaskEditor.ConfigWrap> = emptyList(),
            override val workIds: Set<WorkId> = setOf(WorkId.DEFAULT)
    ) : WorkId.State

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreConfigFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreConfigFragmentVDC
    }

    companion object {
        internal val TAG = App.logTag("Task", "Restore", "Config", "VDC")
    }
}