package eu.darken.bb.processor.core

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.service.ProcessorService
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.putTaskId
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

    fun submit(taskId: Task.Id) {
        val intent = Intent(context, ProcessorService::class.java)
        intent.putTaskId(taskId)
        context.startService(intent)
    }

    fun submit(task: Task) = submit(task.taskId)

    companion object {
        private val TAG = logTag("Processor", "Control")
    }
}