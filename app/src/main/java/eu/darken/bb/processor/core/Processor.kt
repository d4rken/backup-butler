package eu.darken.bb.processor.core

import eu.darken.bb.common.progress.Progress
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.results.TaskResult

interface Processor {

    fun process(task: Task): TaskResult

    interface Factory<T : Processor> {
        fun create(progressParent: Progress.Client): T
    }
}