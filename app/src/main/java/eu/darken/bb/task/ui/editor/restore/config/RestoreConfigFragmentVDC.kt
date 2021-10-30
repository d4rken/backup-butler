package eu.darken.bb.task.ui.editor.restore.config

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.saf.SAFGateway
import eu.darken.bb.common.files.ui.picker.PathPicker
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.rx.withScopeVDC
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RestoreConfigFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val safGateway: SAFGateway,
    private val processorControl: ProcessorControl
) : SmartVDC() {
    private val navArgs by handle.navArgs<RestoreConfigFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorObs = taskBuilder.task(taskId)
        .observeOn(Schedulers.computation())
        .filter { it.editor != null }
        .map { it.editor as SimpleRestoreTaskEditor }

    private val dataObs = editorObs.flatMap { it.editorData }
    private val configWraps = editorObs.flatMap { it.configWraps }

    private val summaryStater = Stater { SummaryState() }
    val summaryState = summaryStater.liveData

    private val configStater = Stater { ConfigState() }
    val configState = configStater.liveData

    val openPickerEvent = SingleLiveEvent<PathPicker.Options>()

    val errorEvent = SingleLiveEvent<Throwable>()

    val finishEvent = SingleLiveEvent<Any>()

    init {
        // Config wraps for item types (defaults, e.g. default for file store)
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

        // Config wraps for specific items
        configWraps
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
        editorObs.firstOrError()
            .observeOn(Schedulers.computation())
            .concatMap { editor ->
                if (target == null) {
                    editor.updateDefaultConfig(config)
                } else {
                    editor.updateCustomConfig(target) { config }
                }
            }
            .subscribe()
    }

    fun pathAction(configWrapper: SimpleRestoreTaskEditor.FilesConfigWrap, target: Backup.Id) {
        Timber.tag(TAG).d("updatePath(generator=%s, target=%s)", configWrapper, target)
        openPickerEvent.postValue(PathPicker.Options(
            startPath = configWrapper.currentPath,
            payload = Bundle().apply { putParcelable("backupId", target) }
        ))
    }

    fun updatePath(result: PathPicker.Result) {
        Timber.tag(TAG).d("updatePath(result=%s)", result)
        if (result.isCanceled) return
        if (result.isFailed) {
            errorEvent.postValue(result.error!!)
            return
        }
        result.options.payload.classLoader = this.javaClass.classLoader
        val backupId: Backup.Id = result.options.payload.getParcelable("backupId")!!
        editorObs.firstOrError()
            .observeOn(Schedulers.computation())
            .concatMap { it.updatePath(backupId, result.selection!!.first()) }
            .subscribe()
    }

    private fun save(execute: Boolean) {
        editorObs.latest()
            .observeOn(Schedulers.computation())
            .flatMap { it.updateOneTime(execute) }
            .flatMap { taskBuilder.save(taskId) }
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

    companion object {
        internal val TAG = logTag("Task", "Restore", "Config", "VDC")
    }
}