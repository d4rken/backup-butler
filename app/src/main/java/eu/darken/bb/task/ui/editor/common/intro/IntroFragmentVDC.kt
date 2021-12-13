package eu.darken.bb.task.ui.editor.common.intro

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class IntroFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    taskBuilder: TaskBuilder,
    dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<IntroFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorObs = taskBuilder.task(taskId)
        .map { it.editor }

    val state = editorObs.flatMapConcat { it.editorData }
        .asLiveData2()

    fun updateTaskName(name: CharSequence) = launch {
        editorObs.first().updateLabel(name.toString())
    }

    fun onContinue() {
        IntroFragmentDirections.actionIntroFragmentToSourcesFragment(navArgs.taskId).navVia(this)
    }
}