package eu.darken.bb.task.ui.editor.common.intro

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder

class IntroFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
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

    @AssistedFactory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): IntroFragmentVDC
    }
}