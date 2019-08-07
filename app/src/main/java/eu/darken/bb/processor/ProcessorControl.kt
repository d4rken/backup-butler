package eu.darken.bb.processor

import android.content.Context
import android.content.Intent
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.main.core.service.BackupService
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.putTaskId
import javax.inject.Inject

@PerApp
class ProcessorControl @Inject constructor(
        @AppContext private val context: Context
) {

    fun submit(taskId: Task.Id) {
        val intent = Intent(context, BackupService::class.java)
        intent.putTaskId(taskId)
        context.startService(intent)
    }

    fun submit(task: Task) = submit(task.taskId)
}