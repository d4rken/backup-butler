package eu.darken.bb.processor.core

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.worker.putParcelable
import eu.darken.bb.processor.core.execution.ProcessorWorker
import eu.darken.bb.task.core.Task
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessorControl @Inject constructor(
    @ApplicationContext private val context: Context,
    @AppScope private val scope: CoroutineScope
) {
    private val progressHostPub = DynamicStateFlow<Progress.Host?>(TAG, scope) { null }
    val progressHost = progressHostPub.flow

    internal fun updateProgressHost(progressHost: Progress.Host?) {
        progressHostPub.updateAsync { progressHost }
    }

    fun submit(request: ProcessorRequest) {
        log(TAG) { "submit(request=$request)" }

        val workerData = Data.Builder().apply {
            putParcelable(ProcessorWorker.WKEY_TASK_REQUEST, request)
        }.build()
        log(TAG) { "Worker data: $workerData" }

        val workRequest = OneTimeWorkRequestBuilder<ProcessorWorker>()
            .setInputData(workerData)
            .build()
        log(TAG) { "Worker request: $workRequest" }

        val operation = WorkManager.getInstance(context).enqueue(workRequest)
        log(TAG) { "Request queued: $operation" }
    }

    fun submit(taskId: Task.Id) = submit(ProcessorRequest(taskId = taskId))

    fun submit(task: Task) = submit(task.taskId)

    companion object {
        private val TAG = logTag("Processor", "Control")
    }
}