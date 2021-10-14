package eu.darken.bb.task.ui.editor.common.intro

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.rx.asLiveData
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import javax.inject.Inject

@HiltViewModel
class IntroFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    private val taskBuilder: TaskBuilder
) : VDC() {
    private val navArgs by handle.navArgs<IntroFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId
    private val editorObs = taskBuilder.task(taskId)
        .filter { it.editor != null }
        .map { it.editor!! }
    val navEvents = SingleLiveEvent<NavDirections>()

    val state = editorObs.flatMap { it.editorData }
        .asLiveData()

    fun updateTaskName(name: CharSequence) {
        editorObs.blockingFirst().updateLabel(name.toString())
    }

    fun onContinue() {
        navEvents.postValue(IntroFragmentDirections.actionIntroFragmentToSourcesFragment(navArgs.taskId))
    }
}