package eu.darken.bb.quickmode.core.files

import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.flow.DynamicStateFlow
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
class FilesQuickMode @Inject constructor(
    moshi: Moshi,
    private val settings: QuickModeSettings,
    private val taskRepo: TaskRepo,
    private val taskBuilder: TaskBuilder,
    private val generatorBuilder: GeneratorBuilder,
    private val processorControl: ProcessorControl,
    @AppScope private val appScope: CoroutineScope,
) {

    private val filesConfigAdapter = moshi.adapter(FilesQuickModeConfig::class.java)

    val state = DynamicStateFlow(TAG, appScope) {
        settings.rawConfigFiles?.let { filesConfigAdapter.fromJson(it) } ?: FilesQuickModeConfig()
    }

    init {
        state.flow
            .onEach {
                settings.rawConfigFiles = filesConfigAdapter.toJson(it)
            }
            .launchIn(appScope)
    }

    suspend fun reset(type: QuickMode.Type): QuickMode.Config {
        log(TAG) { "Resetting QuickMode: $type" }
        var oldData: QuickMode.Config? = null
        state.updateBlocking {
            oldData = this
            FilesQuickModeConfig()
        }
        return oldData!!
    }

    suspend fun launchBackup(selection: Set<APath>) {
        // TODO multiple paths per generator?
        val generatorIds = selection.map { selPath ->
            val editorData = generatorBuilder.getEditor(type = Backup.Type.FILES)
            (editorData.editor as FilesSpecGeneratorEditor).apply {
                setSingleUse(true)
                updatePath(selPath)
            }
            generatorBuilder.save(editorData.generatorId).generatorId
        }
        val quickModeConfig = state.flow.first()

        val taskBuilderData = taskBuilder.getEditor(type = Task.Type.BACKUP_SIMPLE)
        (taskBuilderData.editor as SimpleBackupTaskEditor).apply {
            // TODO implement this within the processor
            // TODO hide one time task from the tasks list
            generatorIds.forEach { addGenerator(it) }
            setSingleUse(true)
            quickModeConfig.storageIds.forEach { addStorage(it) }
        }

        val task = taskBuilder.save(taskBuilderData.taskId)
        processorControl.submit(task.taskId)
        state.updateBlocking {
            copy(lastTaskId = task.taskId)
        }
    }

    companion object {
        private val TAG = logTag("QuickMode", "Repo")
    }

}