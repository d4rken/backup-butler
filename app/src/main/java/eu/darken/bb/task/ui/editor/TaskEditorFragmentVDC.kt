package eu.darken.bb.task.ui.editor

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TaskEditorFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val reqMan: RequirementsManager,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs = handle.navArgs<TaskEditorFragmentArgs>()

    private val args: TaskEditorArgs = navArgs.value.args
    private val taskId: Task.Id = run {
        log(TAG) { "navArgs=${navArgs.value}" }
        val handleKey = "newId"
        when {
            handle.contains(handleKey) -> handle.get<Task.Id>(handleKey)!!
            navArgs.value.args.taskId != null -> navArgs.value.args.taskId!!
            else -> Task.Id().also {
                // ID was null, we create a new one
                handle.set(handleKey, it)
            }
        }
    }
    private val taskBuilderFlow = flow { emit(taskBuilder.getEditor(taskId, args.taskType)) }
        .flatMapConcat { taskBuilder.task(it.taskId) }

    init {
        taskBuilderFlow
            .map { taskData ->
                val reqs = reqMan.reqsFor(taskData.taskType)

                if (reqs.all { it.satisfied }) {
                    when (taskData.taskType) {
                        Task.Type.BACKUP_SIMPLE -> TaskEditorFragmentDirections.actionTaskEditorFragmentToIntroFragment(
                            taskId = taskId
                        )
                        Task.Type.RESTORE_SIMPLE -> {
                            val restoreData =
                                taskData.editor!!.editorData.first() as SimpleRestoreTaskEditor.Data
                            if (restoreData.backupTargets.size == 1) {
                                TaskEditorFragmentDirections.actionTaskEditorFragmentToRestoreConfigFragment(
                                    taskId = taskId
                                )
                            } else {
                                TaskEditorFragmentDirections.actionTaskEditorFragmentToRestoreSourcesFragment(
                                    taskId = taskId
                                )
                            }
                        }
                    }
                } else {
                    TaskEditorFragmentDirections.actionTaskEditorFragmentToRequirementsFragment(
                        taskId = taskId
                    )
                }
            }
            .onEach { it.navVia(this@TaskEditorFragmentVDC) }
            .launchInViewModel()
    }

    companion object {
        val TAG = logTag("Task", "Editor", "VDC")
    }
}