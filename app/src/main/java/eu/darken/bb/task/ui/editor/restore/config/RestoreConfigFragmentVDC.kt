package eu.darken.bb.task.ui.editor.restore.config

import android.os.Bundle
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
import eu.darken.bb.common.file.SAFGateway
import eu.darken.bb.common.file.picker.APathPicker
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.processor.core.ProcessorControl
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
        private val safGateway: SAFGateway,
        private val processorControl: ProcessorControl
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

    val errorEvent = SingleLiveEvent<Throwable>()

    val finishEvent = SingleLiveEvent<Any>()

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
                                isLoading = false
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
                    val issueCount = customConfigs.fold(0, { a, conf -> a + if (!conf.isValid) 1 else 0 })
                    summaryStater.update { it.copy(configsWithIssues = issueCount) }
                    configStater.update { state ->
                        state.copy(
                                customConfigs = customConfigs.sortedBy { it.backupInfoOpt!!.backupId.idString }.toList(),
                                isLoading = false
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
        Timber.tag(TAG).d("updatePath(generator=%s, target=%s)", configWrapper, target)
        openPickerEvent.postValue(APathPicker.Options(
                startPath = configWrapper.currentPath,
                payload = Bundle().apply { putParcelable("backupId", target) }
        ))
    }

    fun updatePath(result: APathPicker.Result) {
        Timber.tag(TAG).d("updatePath(result=%s)", result)
        if (result.isCanceled) return
        if (result.isFailed) {
            errorEvent.postValue(result.error)
            return
        }
        result.options.payload.classLoader = this.javaClass.classLoader
        val backupId: Backup.Id = result.options.payload.getParcelable("backupId")!!
        editorObs.firstOrError()
                .flatMap { it.updatePath(backupId, result.selection!!.first()) }
                .subscribe()
    }

    private fun save(execute: Boolean) {
        editorObs.firstOrError()
                .flatMap { it.updateOneTime(execute) }
                .flatMap { taskBuilder.save(taskId) }
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    configStater.update { it.copy(isWorking = true) }
                }
                .subscribe { savedTask ->
                    if (execute) processorControl.submit(savedTask)
                    finishEvent.postValue(Any())
                }
    }

    fun runTask() {
        save(execute = true)
    }

    fun saveTask() {
        save(execute = false)
    }

    data class SummaryState(
            val backupTypes: List<Backup.Type> = emptyList(),
            val customConfigCount: Int = 0,
            val configsWithIssues: Int = 0,
            val isLoading: Boolean = true
    )

    data class ConfigState(
            val defaultConfigs: List<SimpleRestoreTaskEditor.ConfigWrap> = emptyList(),
            val customConfigs: List<SimpleRestoreTaskEditor.ConfigWrap> = emptyList(),
            val isLoading: Boolean = true,
            val isWorking: Boolean = false
    )

    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreConfigFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreConfigFragmentVDC
    }

    companion object {
        internal val TAG = App.logTag("Task", "Restore", "Config", "VDC")
    }
}