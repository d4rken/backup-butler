package eu.darken.bb.task.core.results

import android.content.Context
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.task.core.Task
import java.util.*

data class SimpleResult constructor(
        override val resultId: Task.Result.Id,
        override val taskId: Task.Id,
        override val taskType: Task.Type,
        override val label: String,
        override val state: Task.Result.State,
        override val startedAt: Date,
        override val duration: Long,
        override val primary: String? = null,
        override val secondary: String? = null,
        override val extra: String? = null,
        override val taskLog: List<String>? = null,
        val error: Throwable? = null
) : Task.Result {

    class Builder constructor(private val context: Context) : Task.Result.Builder<SimpleResult> {
        private var taskId: Task.Id? = null
        private var taskType: Task.Type? = null
        private var taskName: String? = null
        private var state: Task.Result.State? = null
        private var startedAt: Date = Date()
        private var duration: Long = 60 * 1000L
        private var primary: String? = null
        private var secondary: String? = null
        private var taskLog: List<String>? = null
        private var extra: String? = null
        private var error: Throwable? = null

        fun forTask(task: Task) = apply {
            this.taskId = task.taskId
            this.taskType = task.taskType
            this.taskName = task.label
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

        override fun build(context: Context) = SimpleResult(
                resultId = Task.Result.Id(),
                taskId = taskId!!,
                taskType = taskType!!,
                label = taskName!!,
                state = state!!,
                startedAt = startedAt,
                duration = duration,
                primary = primary,
                secondary = secondary,
                taskLog = taskLog,
                extra = extra
        )
    }
}