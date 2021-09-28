package eu.darken.bb.processor.core.processors

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.common.AString
import eu.darken.bb.common.SharedHolder
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.progress.updateProgressTertiary
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.SimpleResult
import eu.darken.bb.task.core.results.TaskResult
import timber.log.Timber

abstract class SimpleBaseProcessor constructor(
    val context: Context,
    val progressParent: Progress.Client
) : Processor, Progress.Client, SharedHolder.HasKeepAlive<Any> {

    val progressChild = object : Progress.Client {
        override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
            progressParent.updateProgress { parent ->
                val oldChild = parent.child ?: Progress.Data()
                val newChild = update.invoke(oldChild)
                parent.copy(child = newChild)
            }
        }
    }
    override val keepAlive = SharedHolder.createKeepAlive(TAG)

    val resultBuilder = SimpleResult.Builder()

    override fun process(task: Task): TaskResult {
        Timber.tag(TAG).i("Processing task: %s", task)
        try {
            resultBuilder.forTask(task)
            resultBuilder.startNow()

            keepAlive.get().use {
                doProcess(task)
            }

            resultBuilder.sucessful()
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "Task failed: %s", task)
            resultBuilder.error(context, exception)
        } finally {
            onCleanup()
            progressParent.updateProgressSecondary(R.string.progress_working_label)
            progressParent.updateProgressTertiary(AString.EMPTY)
            progressParent.updateProgressCount(Progress.Count.Indeterminate())
            progressParent.updateProgress { it.copy(child = null) }
        }
        return resultBuilder.build(context)
    }

    abstract fun doProcess(task: Task)

    open fun onCleanup() {

    }

    companion object {
        private val TAG = logTag("Processor", "BaseProcessor")
    }
}