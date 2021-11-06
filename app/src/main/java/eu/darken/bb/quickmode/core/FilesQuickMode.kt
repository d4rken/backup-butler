package eu.darken.bb.quickmode.core

import com.squareup.moshi.Moshi
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorBuilder
import eu.darken.bb.backup.core.files.FilesSpecGeneratorEditor
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.backup.SimpleBackupTaskEditor
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Singles
import io.reactivex.rxjava3.schedulers.Schedulers
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
) {

    private val filesConfigAdapter = moshi.adapter(FilesQuickModeConfig::class.java)

    val hotData = HotData {
        settings.rawConfigFiles?.let { filesConfigAdapter.fromJson(it) } ?: FilesQuickModeConfig()
    }

    init {
        hotData.data.subscribe {
            settings.rawConfigFiles = filesConfigAdapter.toJson(it)
        }
    }

    fun reset(type: QuickMode.Type): Single<QuickMode.Config> = hotData
        .updateRx { FilesQuickModeConfig() }
        .doOnSubscribe { log(TAG) { "Resetting QuickMode: $type" } }
        .observeOn(Schedulers.computation())
        .map { it.oldValue }

    data class TaskShot(
        val generatorIds: Collection<Generator.Id>,
        val taskEditor: SimpleBackupTaskEditor,
        val quickConfig: FilesQuickModeConfig
    )

    fun launchBackup(selection: Set<APath>) {
        // TODO mark these as one shot, hide them from UI
        val generators = selection.map { selPath ->
            generatorBuilder
                .getEditor(type = Backup.Type.FILES)
                .flatMap {
                    it.editor as FilesSpecGeneratorEditor
                    it.editor
                        .updatePath(selPath)
                        .andThen(generatorBuilder.save(it.generatorId))
                }
                .map { it.generatorId }
        }

        val taskShot = Single
            .zip(generators) { it.toList() as List<Generator.Id> }
            .flatMap { generatorIds ->
                Singles
                    .zip(
                        taskBuilder.getEditor(type = Task.Type.BACKUP_SIMPLE)
                            .map { (it.editor as SimpleBackupTaskEditor) },
                        hotData.data.firstOrError(),
                    )
                    .map { (taskData, quickModeConfig) ->
                        TaskShot(generatorIds, taskData, quickModeConfig)
                    }
            }

        taskShot
            .flatMap { (generatorIds, editor, config) ->
                editor.updateOneTime(true).blockingGet()
                generatorIds.forEach { editor.addGenerator(it) }
                config.storageIds.forEach { editor.addStorage(it) }
                taskBuilder.save(editor.taskId)
            }
            .subscribe { task ->
                processorControl.submit(task.taskId)
            }

    }

    companion object {
        private val TAG = logTag("QuickMode", "Repo")
    }

}