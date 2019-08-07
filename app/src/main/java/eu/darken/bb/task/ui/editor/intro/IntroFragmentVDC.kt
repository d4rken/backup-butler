package eu.darken.bb.task.ui.editor.intro

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.rx.toLiveData
import eu.darken.bb.task.core.BackupTask
import eu.darken.bb.task.core.DefaultBackupTask
import eu.darken.bb.task.core.TaskBuilder
import io.reactivex.schedulers.Schedulers

class IntroFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: BackupTask.Id,
        private val taskBuilder: TaskBuilder
) : VDC() {

    val state = taskBuilder.task(taskId).toLiveData()

    fun updateTaskName(name: CharSequence) {
        taskBuilder
                .update(taskId) {
                    it as DefaultBackupTask
                    it.copy(taskName = name.toString())
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    @AssistedInject.Factory
    interface Factory : VDCFactory<IntroFragmentVDC> {
        fun create(handle: SavedStateHandle, taskId: BackupTask.Id): IntroFragmentVDC
    }
}