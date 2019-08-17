package eu.darken.bb.task.ui.editor.intro

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
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

    val state = editorObs.flatMap { it.config }
            .toLiveData()

    fun updateTaskName(name: CharSequence) {
        editorObs.blockingFirst().updateLabel(name.toString())
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): IntroFragmentVDC
    }
}