package eu.darken.bb.quickmode.core.apps

import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.app.AppSpecGeneratorEditor
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.pkgs.picker.core.PickedPkg
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.quickmode.core.QuickMode
import eu.darken.bb.quickmode.core.QuickModeSettings
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsQuickMode @Inject constructor(
    private val taskRepo: TaskRepo,
    private val moshi: Moshi,
    private val settings: QuickModeSettings,
    private val taskBuilder: TaskBuilder,
    private val generatorBuilder: GeneratorBuilder,
    private val processorControl: ProcessorControl,
    @AppScope private val appScope: CoroutineScope,
) {

    private val adapter = moshi.adapter(AppsQuickModeConfig::class.java)

    val state = DynamicStateFlow(TAG, appScope) {
        settings.rawConfigApps?.let { adapter.fromJson(it) } ?: AppsQuickModeConfig()
    }

    init {
        state.flow
            .onEach { settings.rawConfigApps = adapter.toJson(it) }
            .launchIn(appScope)
    }

    suspend fun reset(): QuickMode.Config {
        log(TAG) { "Resetting QuickMode" }
        var oldValue: QuickMode.Config? = null
        state.updateBlocking {
            oldValue = this
            AppsQuickModeConfig()
        }
        return oldValue!!
    }

    suspend fun launchBackup(selection: Set<PickedPkg>) {
        val editorData = generatorBuilder.getEditor(type = Backup.Type.APP)
        (editorData.editor as AppSpecGeneratorEditor).apply {
            update { data ->
                data.copy(
                    packagesIncluded = selection.map { it.pkg }.toSet(),
                    isSingleUse = true
                )
            }
        }

        val generatorId = generatorBuilder.save(editorData.generatorId).generatorId

        val taskBuilderData = taskBuilder.getEditor(type = Task.Type.BACKUP_SIMPLE)
        (taskBuilderData.editor as SimpleBackupTaskEditor).apply {
            setSingleUse(true)
            addGenerator(generatorId)
        }

        val quickModeConfig = state.flow.first()

        quickModeConfig.storageIds.forEach { taskBuilderData.editor.addStorage(it) }

        val task = taskBuilder.save(taskBuilderData.taskId)
        processorControl.submit(task.taskId)
    }

    companion object {
        private val TAG = logTag("QuickMode", "Repo", "Apps")
    }

}