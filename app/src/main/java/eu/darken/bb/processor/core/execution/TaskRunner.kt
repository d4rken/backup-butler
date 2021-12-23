package eu.darken.bb.processor.core.execution

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.INFO
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressPrimary
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.ProcessorControl
import eu.darken.bb.processor.core.ProcessorRequest
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskRepo
import eu.darken.bb.task.core.results.SimpleResult
import eu.darken.bb.task.core.results.TaskResult
import eu.darken.bb.task.core.results.TaskResultRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRunner @Inject constructor(
    @ApplicationContext private val context: Context,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val taskRepo: TaskRepo,
    private val processorControl: ProcessorControl,
    private val resultRepo: TaskResultRepo,
    private val processorFactories: @JvmSuppressWildcards Map<Task.Type, Processor.Factory<out Processor>>,
) : Progress.Host, Progress.Client {

    private val progressUpdater by lazy { DynamicStateFlow("$TAG:Progress", processorScope) { Progress.Data() } }

    init {
        log(TAG) { "init()" }
    }

    override val progress: Flow<Progress.Data> by lazy { progressUpdater.flow }
    override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) {
        progressUpdater.updateAsync(onUpdate = update)
    }

    suspend fun execute(request: ProcessorRequest, runAttemptCount: Int): TaskResult {
        processorControl.updateProgressHost(this)
        updateProgressPrimary(R.string.progress_preparing_label)

        val task = taskRepo.get(request.taskId) ?: throw IllegalArgumentException("Can't find task for $request")

        if (runAttemptCount > request.maxRunAttempts) {
            // If the app is killed while work is on-going, it will be retried
            // A stuck task could lead to dozens of workers being retried
            throw IllegalStateException("runAttemptCount ($runAttemptCount) exceeded retry attempts: $request")
        }

        val taskResult = try {
            runTask(task)
        } catch (e: Throwable) {
            log(TAG, ERROR) { "Task execution failed for $request:\n${e.asLog()}" }
            SimpleResult.Builder().forTask(task).error(context, e).build(context)
        }

        processorControl.updateProgressHost(null)

        return taskResult
    }

    private suspend fun runTask(task: Task): TaskResult {
        updateProgressPrimary(R.string.progress_loading_task_label)

        log(TAG, INFO) { "Processing task: $task" }

        val processor = processorFactories.getValue(task.taskType).create(this)
        val taskResult = processor.process(task)
        resultRepo.submitResult(taskResult)

        if (task.isSingleUse) {
            log(TAG, INFO) { "Removing one-time-task: $task" }
            taskRepo.remove(task.taskId)
        }
        if (taskResult.state == TaskResult.State.SUCCESS) {
            log(TAG, INFO) { "Successfully processed $task: $taskResult" }
        } else {
            log(TAG, ERROR) { "Error while processing $task: $taskResult" }
        }
        return taskResult
    }

    companion object {
        val TAG = logTag("Processor", "Runner")
    }
}