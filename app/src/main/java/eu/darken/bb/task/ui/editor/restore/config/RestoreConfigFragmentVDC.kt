package eu.darken.bb.task.ui.editor.restore.config

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.ui.picker.PathPickerOptions
import eu.darken.bb.common.files.ui.picker.PathPickerResult
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RestoreConfigFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val safGateway: SAFGateway,
    private val processorControl: ProcessorControl,
    private val dispatcherProvider: DispatcherProvider
) : Smart2VDC(dispatcherProvider) {
    private val navArgs by handle.navArgs<RestoreConfigFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorFlow = taskBuilder.task(taskId)
        .filterNotNull()
        .map { it.editor as SimpleRestoreTaskEditor }

    private val dataFlow = editorFlow.flatMapConcat { it.editorData }
    private val configWraps = editorFlow.flatMapConcat { it.configWraps }

    private val summaryStater = DynamicStateFlow(TAG, vdcScope) { SummaryState() }
    val summaryState = summaryStater.asLiveData2()

    private val configStater = DynamicStateFlow(TAG, vdcScope) { ConfigState() }
    val configState = configStater.asLiveData2()

    val openPickerEvent = SingleLiveEvent<PathPickerOptions>()

    init {
        // Config wraps for item types (defaults, e.g. default for file store)
        dataFlow
            .onEach { data ->
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

                summaryStater.updateBlocking {
                    copy(
                        backupTypes = data.defaultConfigs.values.map { it.restoreType },
                        customConfigCount = customCount,
                        isLoading = false
                    )
                }
                configStater.updateBlocking {
                    copy(
                        defaultConfigs = wrappedDefaults
                    )
                }
            }
            .launchInViewModel()

        // Config wraps for specific items
        configWraps
            .onEach { customConfigs ->
                val issueCount = customConfigs.fold(0, { a, conf -> a + if (!conf.isValid) 1 else 0 })
                summaryStater.updateBlocking { copy(configsWithIssues = issueCount) }
                configStater.updateBlocking {
                    copy(
                        customConfigs = customConfigs.sortedBy { it.backupInfoOpt!!.backupId.idString }.toList(),
                        isLoading = false
                    )
                }
            }
            .launchInViewModel()
    }

    fun updateConfig(config: Restore.Config, target: Backup.Id? = null) = launch {
        val editor = editorFlow.first()
        if (target == null) {
            editor.updateDefaultConfig(config)
        } else {
            editor.updateCustomConfig(target) { config }
        }
    }

    fun pathAction(configWrapper: SimpleRestoreTaskEditor.FilesConfigWrap, target: Backup.Id) {
        log(TAG) { "updatePath(generator=$configWrapper, target=$target)" }
        openPickerEvent.postValue(PathPickerOptions(
            startPath = configWrapper.currentPath,
            payload = Bundle().apply { putParcelable("backupId", target) }
        ))
    }

    fun updatePath(result: PathPickerResult) = launch {
        log(TAG) { "updatePath(result=$result)" }
        if (result.isCanceled) return@launch
        if (result.isFailed) {
            errorEvents.postValue(result.error!!)
            return@launch
        }
        result.options.payload.classLoader = this.javaClass.classLoader
        val backupId: Backup.Id = result.options.payload.getParcelable("backupId")!!
        val editor = editorFlow.first()
        editor.updatePath(backupId, result.selection!!.first())
    }

    private fun save(execute: Boolean) = launch {
        configStater.updateBlocking { copy(isWorking = true) }
        val editor = editorFlow.first()
        editor.setSingleUse(execute)

        val savedTask = taskBuilder.save(taskId)

        if (execute) processorControl.submit(savedTask)

        navEvents.postValue(null)
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

    companion object {
        internal val TAG = logTag("Task", "Restore", "Config", "VDC")
    }
}