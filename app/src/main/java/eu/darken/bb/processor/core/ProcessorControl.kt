package eu.darken.bb.processor.core

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.common.HotData
import eu.darken.bb.common.Opt
import eu.darken.bb.common.opt
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.service.ProcessorService
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.putTaskId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessorControl @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val progressHostPub = HotData<Opt<Progress.Host>> { Opt() }
    val progressHost = progressHostPub.data

    internal fun updateProgressHost(progressHost: Progress.Host?) {
        progressHostPub.update { progressHost.opt() }
    }

    fun submit(taskId: Task.Id) {
        val intent = Intent(context, ProcessorService::class.java)
        intent.putTaskId(taskId)
        context.startService(intent)
    }

    fun submit(task: Task) = submit(task.taskId)
}