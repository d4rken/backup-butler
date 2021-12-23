package eu.darken.bb.processor.core.execution

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import eu.darken.bb.common.debug.logging.Logging.Priority.ERROR
import eu.darken.bb.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.bb.common.debug.logging.asLog
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.worker.getParcelable
import eu.darken.bb.processor.core.ProcessorComponent
import eu.darken.bb.processor.core.ProcessorCoroutineScope
import eu.darken.bb.processor.core.ProcessorRequest
import eu.darken.bb.processor.ui.notifications.ProcessorNotifications
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltWorker class ProcessorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    processorComponentProvider: ProcessorComponent.Builder,
    private val notifications: ProcessorNotifications,
) : CoroutineWorker(context, params) {

    private val workerScope = ProcessorCoroutineScope()
    private val processorCompoent = processorComponentProvider
        .coroutineScope(workerScope)
        .build()
    private val runner by lazy {
        EntryPoints.get(processorCompoent, ProcessorWorkerEntryPoint::class.java).taskRunner()
    }
    private var finishedWithError = false

    init {
        log(TAG, VERBOSE) { "init(): workerId=$id" }
    }

    override suspend fun doWork(): Result = try {
        val request = try {
            inputData.getParcelable<ProcessorRequest>(WKEY_TASK_REQUEST)!!
        } catch (e: Exception) {
            throw IllegalArgumentException("Worker executed without valid inputData: $inputData", e)
        }

        val start = System.currentTimeMillis()
        log(TAG, VERBOSE) { "Executing $request now (runAttemptCount=$runAttemptCount)" }

        notifications
            .getInfos(runner)
            .onEach { setForeground(it) }
            .launchIn(workerScope)

        val result = runner.execute(request, runAttemptCount)

        val duration = System.currentTimeMillis() - start

        log(TAG, VERBOSE) { "Execution finished after ${duration}ms, $request -> $result" }

        Result.success(inputData)
    } catch (e: Throwable) {
        log(TAG, ERROR) { "Excecution failed:\n${e.asLog()}" }
        finishedWithError = true
        // TODO update result?
        Result.failure(inputData)
    } finally {
        this.workerScope.cancel("Worker finished (withError?=$finishedWithError).")
    }

    companion object {
        val TAG = logTag("Processor", "Worker")
        const val WKEY_TASK_REQUEST: String = "worker.processorRequest"
    }
}

@InstallIn(ProcessorComponent::class)
@EntryPoint
interface ProcessorWorkerEntryPoint {
    fun taskRunner(): TaskRunner
}