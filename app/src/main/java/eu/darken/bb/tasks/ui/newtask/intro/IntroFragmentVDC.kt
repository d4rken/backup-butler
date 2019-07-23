package eu.darken.bb.tasks.ui.newtask.intro

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.tasks.core.DefaultBackupTask
import eu.darken.bb.tasks.core.TaskBuilder
import java.util.*

class IntroFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: UUID,
        private val taskBuilder: TaskBuilder
) : VDC() {

    val state = taskBuilder.task(taskId).toLiveData()

    fun updateTaskName(name: CharSequence) {
        taskBuilder.update(taskId) {
            it as DefaultBackupTask
            it.copy(taskName = name.toString())
        }
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: UUID): IntroFragmentVDC
    }
}