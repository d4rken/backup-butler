package eu.darken.bb.task.core.backup

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import java.util.*

data class SimpleBackupTask(
        override val taskName: String,
        override val taskId: Task.Id,
        override val sources: Set<Generator.Id>,
        override val destinations: Set<Storage.Id>
) : Task.Backup {

    override val taskType: Task.Type = Task.Type.BACKUP_SIMPLE

    override fun getDescription(context: Context): String {
        return context.getString(R.string.default_backuptask_description_x_sources_x_destinations, sources.size, destinations.size)
    }

    data class Result constructor(
            override val resultId: Task.Result.Id,
            override val taskId: Task.Id,
            override val taskType: Task.Type,
            override val taskName: String,
            override val state: Task.Result.State,
            override val startedAt: Date,
            override val duration: Long,
            override val primary: String? = null,
            override val secondary: String? = null,
            override val extra: String? = null,
            val error: Throwable? = null
    ) : Task.Result {

        class Builder constructor(private val context: Context) {
            private var taskId: Task.Id? = null
            private var taskType: Task.Type? = null
            private var taskName: String? = null
            private var state: Task.Result.State? = null
            private var startedAt: Date = Date()
            private var duration: Long = 60 * 1000L
            private var primary: String? = null
            private var secondary: String? = null
            private var extra: String? = null
            private var error: Throwable? = null

            fun forTask(task: Task) = apply {
                this.taskId = task.taskId
                this.taskType = task.taskType
                this.taskName = task.taskName
            }

            fun startNow() = apply {
                this.startedAt = Date()
            }

            fun finished() = apply {
                this.duration = System.currentTimeMillis() - startedAt.time
            }

            fun sucessful() = apply {
                this.state = Task.Result.State.SUCCESS
                finished()
            }

            fun error(error: Throwable) = apply {
                this.state = Task.Result.State.ERROR
                this.primary = error.javaClass.name
                this.secondary = error.tryLocalizedErrorMessage(context)
                finished()
            }

            fun primary(primary: String?) = apply {
                this.primary = primary
            }

            fun secondary(secondary: String?) = apply {
                this.secondary = secondary
            }

            fun extra(extra: String?) = apply {
                this.extra = extra
            }

            fun createResult() = Result(
                    resultId = Task.Result.Id(),
                    taskId = taskId!!,
                    taskType = taskType!!,
                    taskName = taskName!!,
                    state = state!!,
                    startedAt = startedAt,
                    duration = duration,
                    primary = primary,
                    secondary = secondary,
                    extra = extra
            )
        }
    }

}