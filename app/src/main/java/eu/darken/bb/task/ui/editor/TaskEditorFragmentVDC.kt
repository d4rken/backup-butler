package eu.darken.bb.task.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
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

    // TODO create on demand like storageEditor?
    private val navArgs = handle.navArgs<TaskEditorFragmentArgs>()

    private val args: TaskEditorArgs = navArgs.value.args

    private val taskObs = taskBuilder.task(args.taskId).observeOn(Schedulers.computation())

    private val editorObs = taskObs
        .filter { it.editor != null }
        .map { it.editor!! }

    private val editorDataObs = editorObs.flatMap { it.editorData }

    val navEvents = SingleLiveEvent<NavDirections>()

    init {
        taskObs
            .concatMapSingle { taskData ->
                reqMan.reqsFor(taskData.taskType).map { reqs ->
                    if (reqs.all { it.satisfied }) {
                        when (taskData.taskType) {
                            Task.Type.BACKUP_SIMPLE -> TaskEditorFragmentDirections.actionTaskEditorFragmentToIntroFragment(
                                taskId = args.taskId
                            )
                            Task.Type.RESTORE_SIMPLE -> {
                                val restoreData = editorDataObs.blockingFirst() as SimpleRestoreTaskEditor.Data
                                if (restoreData.backupTargets.size == 1) {
                                    TaskEditorFragmentDirections.actionTaskEditorFragmentToRestoreConfigFragment(
                                        taskId = args.taskId
                                    )
                                } else {
                                    TaskEditorFragmentDirections.actionTaskEditorFragmentToRestoreSourcesFragment(
                                        taskId = args.taskId
                                    )
                                }
                            }
                        }
                    } else {
                        TaskEditorFragmentDirections.actionTaskEditorFragmentToRequirementsFragment(
                            taskId = args.taskId
                        )
                    }
                }
            }
            .subscribe { navEvents.postValue(it) }
    }

//    enum class StepFlow(@IdRes val start: Int) {
//        BACKUP_SIMPLE(R.id.introFragment),
//        RESTORE_SIMPLE(R.id.restoreSourcesFragment),
//        RESTORE_SIMPLE_SINGLE(R.id.restoreConfigFragment);
//    }
}