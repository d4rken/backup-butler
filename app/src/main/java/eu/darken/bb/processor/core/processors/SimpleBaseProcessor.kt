package eu.darken.bb.processor.core.processors

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.CaString
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.progress.updateProgressTertiary
import eu.darken.bb.common.sharedresource.HasSharedResource
import eu.darken.bb.common.sharedresource.SharedResource
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.SimpleResult
import eu.darken.bb.task.core.results.TaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import timber.log.Timber

abstract class SimpleBaseProcessor constructor(
    val context: Context,
    val progressParent: Progress.Client,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : Processor, Progress.Client, HasSharedResource<Any> {

    val progressChild = object : Progress.Client {
        override fun updateProgress(update: suspend (Progress.Data) -> Progress.Data) {
            progressParent.updateProgress { parent ->
                val oldChild = parent.child ?: Progress.Data()
                val newChild = update.invoke(oldChild)
                parent.copy(child = newChild)
            }
        }
    }
    override val sharedResource = SharedResource.createKeepAlive(TAG, processorScope + dispatcherProvider.IO)

    val resultBuilder = SimpleResult.Builder()

    override suspend fun process(task: Task): TaskResult {
        Timber.tag(TAG).i("Processing task: %s", task)
        try {
            resultBuilder.forTask(task)
            resultBuilder.startNow()

            sharedResource.get().use {
                doProcess(task)
            }
            resultBuilder.sucessful()
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "Task failed: %s", task)
            resultBuilder.error(context, exception)
        } finally {
            onCleanup()
            progressParent.updateProgressSecondary(R.string.progress_working_label)
            progressParent.updateProgressTertiary(CaString.EMPTY)
            progressParent.updateProgressCount(Progress.Count.Indeterminate())
            progressParent.updateProgress { it.copy(child = null) }
        }
        return resultBuilder.build(context)
    }

    abstract suspend fun doProcess(task: Task)

    open suspend fun onCleanup() {

    }

    companion object {
        private val TAG = logTag("Processor", "BaseProcessor")
    }
}