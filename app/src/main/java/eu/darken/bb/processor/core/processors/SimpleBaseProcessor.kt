package eu.darken.bb.processor.core.processors

import android.content.Context
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.SimpleResult
import timber.log.Timber

abstract class SimpleBaseProcessor constructor(
        val context: Context,
        val progressParent: Progress.Client
) : Processor {

    val progressChild = object : Progress.Client {
        override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
            progressParent.updateProgress { parent ->
                val oldChild = parent.child ?: Progress.Data()
                val newChild = update.invoke(oldChild)
                parent.copy(child = newChild)
            }
        }
    }

    val resultBuilder = SimpleResult.Builder(context)

    override fun process(task: Task): Task.Result {
        Timber.tag(TAG).i("Processing task: %s", task)
        try {
            resultBuilder.forTask(task)
            resultBuilder.startNow()
            doProcess(task)
            resultBuilder.sucessful()
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "Task failed: %s", task)
            resultBuilder.error(exception)
        } finally {
            onCleanup()
            progressParent.updateProgressSecondary(R.string.progress_working_label)
            progressParent.updateProgressCount(Progress.Count.Indeterminate())
            progressParent.updateProgress { it.copy(child = null) }
        }
        return resultBuilder.build(context)
    }

    abstract fun doProcess(task: Task)

    open fun onCleanup() {

    }

    companion object {
        private val TAG = App.logTag("Processor", "BaseProcessor")
    }
}