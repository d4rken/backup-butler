package eu.darken.bb.task.ui.editor.common.intro

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import javax.inject.Inject

@HiltViewModel
class IntroFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    @Assisted private val taskId: Task.Id,
    private val taskBuilder: TaskBuilder
) : VDC() {
    private val editorObs = taskBuilder.task(taskId)
        .filter { it.editor != null }
        .map { it.editor!! }

    val state = editorObs.flatMap { it.editorData }
        .toLiveData()

    fun updateTaskName(name: CharSequence) {
        editorObs.blockingFirst().updateLabel(name.toString())
    }
}