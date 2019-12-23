package eu.darken.bb.task.core.results

import android.content.Context
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.task.core.Task
import timber.log.Timber
import java.util.*

data class SimpleResult constructor(
        override val resultId: TaskResult.Id,
        override val taskId: Task.Id,
        override val taskType: Task.Type,
        override val label: String,
        override val startedAt: Date,
        override val duration: Long,
        override val state: TaskResult.State,
        override val primary: String? = null,
        override val secondary: String? = null,
        override val extra: String? = null,
        override val subResults: List<SimpleSubResult>
) : TaskResult {

    class Builder : TaskResult.Builder<SimpleResult> {
        private val taskResultId = TaskResult.Id()
        private var taskId: Task.Id? = null
        private var taskType: Task.Type? = null
        private var taskName: String? = null
        private var startedAt: Date = Date()
        private var duration: Long = 60 * 1000L
        private var state: TaskResult.State? = null
        private var primary: String? = null
        private var secondary: String? = null
        private var extra: String? = null
        private var subResults = mutableListOf<SimpleSubResult>()

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
            this.state = TaskResult.State.SUCCESS
            finished()
        }

        fun error(context: Context, error: Throwable) = apply {
            this.state = TaskResult.State.ERROR
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

        override fun build(context: Context): SimpleResult {
            subResults.singleOrNull { it.state == TaskResult.State.ERROR }?.let {
                state = TaskResult.State.ERROR
            }
            if (subResults.isEmpty()) Timber.w("Empty subresults: %s", this)

            return SimpleResult(
                    resultId = taskResultId,
                    taskId = taskId!!,
                    taskType = taskType!!,
                    label = taskName!!,
                    startedAt = startedAt,
                    duration = duration,
                    state = state!!,
                    primary = primary,
                    secondary = secondary,
                    subResults = subResults,
                    extra = extra
            )
        }

        fun addSubResult(subResultBuider: SimpleSubResult.Builder) {
            val subResult = subResultBuider.build(taskResultId)
            subResults.add(subResult)
        }
    }

    data class SimpleSubResult(
            override val subResultId: TaskResult.SubResult.Id,
            override val resultId: TaskResult.Id,
            override val label: String,
            override val state: TaskResult.State,
            override val primary: String?,
            override val secondary: String?,
            override val extra: String?,
            override val taskLog: List<String>?,
            override val IOEvents: List<IOEvent>?
    ) : TaskResult.SubResult {
        class Builder : TaskResult.SubResult.Builder<SimpleSubResult> {
            private var label: String? = null
            private var state: TaskResult.State? = null
            private var primary: String? = null
            private var secondary: String? = null
            private var extra: String? = null
            private var logActions = mutableListOf<IOEvent>()
            private var taskLog: List<String>? = null

            fun label(label: String) = apply {
                this.label = label
            }

            fun sucessful() = apply {
                this.state = TaskResult.State.SUCCESS
            }

            fun error(context: Context, error: Throwable) = apply {
                this.state = TaskResult.State.ERROR
                this.primary = error.javaClass.name
                this.secondary = error.tryLocalizedErrorMessage(context)
            }

            fun addIOInfo(IOEvent: IOEvent) = apply {
                logActions.add(IOEvent)
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

            override fun build(taskResultId: TaskResult.Id): SimpleSubResult {
                return SimpleSubResult(
                        subResultId = TaskResult.SubResult.Id(),
                        resultId = taskResultId,
                        label = label!!,
                        state = state!!,
                        primary = primary,
                        secondary = secondary,
                        extra = extra,
                        IOEvents = logActions,
                        taskLog = taskLog

                )
            }
        }

    }
}