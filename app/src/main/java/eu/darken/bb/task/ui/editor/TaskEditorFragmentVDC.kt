package eu.darken.bb.task.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.core.common.requirements.RequirementsManager
import eu.darken.bb.task.core.restore.SimpleRestoreTaskEditor
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class TaskEditorFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder,
    private val reqMan: RequirementsManager
) : SmartVDC() {

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
    private val taskObs = taskBuilder
        .load(taskId)
        .observeOn(Schedulers.computation())
        .switchIfEmpty(taskBuilder.createEditor(taskId, args.taskType))
        .flatMapObservable { taskBuilder.task(it.taskId) }

    val navEvents = SingleLiveEvent<NavDirections>()

    init {
        taskObs
            .concatMapSingle { taskData ->
                reqMan.reqsFor(taskData.taskType).map { reqs ->
                    if (reqs.all { it.satisfied }) {
                        when (taskData.taskType) {
                            Task.Type.BACKUP_SIMPLE -> TaskEditorFragmentDirections.actionTaskEditorFragmentToIntroFragment(
                                taskId = taskId
                            )
                            Task.Type.RESTORE_SIMPLE -> {
                                val restoreData =
                                    taskData.editor!!.editorData.blockingFirst() as SimpleRestoreTaskEditor.Data
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
            }
            .subscribe { navEvents.postValue(it) }
    }

    companion object {
        val TAG = logTag("Task", "Editor", "VDC")
    }
}