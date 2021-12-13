package eu.darken.bb.task.ui.editor.common.summary

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.CaString
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.navigation.navArgs
import eu.darken.bb.common.navigation.navVia
import eu.darken.bb.common.smart.Smart2VDC
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskBuilder
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SummaryFragmentVDC @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val taskBuilder: TaskBuilder,
    private val processorControl: ProcessorControl,
) : Smart2VDC(dispatcherProvider) {

    private val navArgs by handle.navArgs<SummaryFragmentArgs>()
    private val taskId: Task.Id = navArgs.taskId

    private val editorFlow = taskBuilder.task(taskId)
        .map { it.editor }

    private val workingState = MutableStateFlow(false)
    private val stater = combine(workingState, editorFlow) { isWorking, editor ->
        val taskSnapshot = editor.snapshot()

        if (isWorking) {
            State.Busy
        } else {
            State.Summary(
                taskType = taskSnapshot.taskType,
                isSingleUse = taskSnapshot.isSingleUse,
                label = taskSnapshot.label,
                description = taskSnapshot.getDescription()
            )
        }
    }
        .onStart { State.Busy }

    val state = stater.asLiveData2()

    private fun save(execute: Boolean) = launch {
        log(TAG) { "save(execute=$execute)" }
        workingState.value = true

        val editor = editorFlow.first()
        editor.setSingleUse(execute)

        val savedTask = taskBuilder.save(taskId)

        if (execute) processorControl.submit(savedTask)

        SummaryFragmentDirections.actionSummaryFragmentToMainNavGraph().navVia(this@SummaryFragmentVDC)
    }

    fun runTask() {
        save(execute = true)
    }

    fun saveTask() {
        save(execute = false)
    }

    sealed class State {

        object Busy : State()

        data class Summary(
            val taskType: Task.Type,
            val isSingleUse: Boolean,
            val label: String,
            val description: CaString
        ) : State()

    }


    companion object {
        private val TAG = logTag("Task", "Summary", "VDC")
    }

}