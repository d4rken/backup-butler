package eu.darken.bb.task.ui.editor.restore

import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SingleLiveEvent
import eu.darken.bb.common.Stater
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragment
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass


class RestoreTaskActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @Assisted private val taskId: Task.Id,
        private val taskBuilder: TaskBuilder
) : SmartVDC() {
    private val stater: Stater<State> = Stater(State(taskId = taskId, step = State.Step.CONFIGS))

    val state = stater.liveData

    val finishActivity = SingleLiveEvent<Boolean>()

    fun dismiss(): Boolean {
        taskBuilder.remove(taskId)
                .subscribeOn(Schedulers.io())
                .subscribe { _ ->
                    finishActivity.postValue(true)
                }
        return true
    }

    data class State(
            val taskId: Task.Id,
            val step: Step,
            val saveable: Boolean = false,
            val existingTask: Boolean = false
    ) {
        enum class Step(
                val stepPos: Int,
                val fragmentClass: KClass<out Fragment>
        ) {
            CONFIGS(0, RestoreConfigFragment::class)
        }
    }


    @AssistedInject.Factory
    interface Factory : VDCFactory<RestoreTaskActivityVDC> {
        fun create(handle: SavedStateHandle, taskId: Task.Id): RestoreTaskActivityVDC
    }
}