package eu.darken.bb.processor.core

import eu.darken.bb.common.progress.Progress
import eu.darken.bb.task.core.Task

interface Processor {

    fun process(task: Task): Task.Result

    interface Factory<T : Processor> {
        fun create(progressParent: Progress.Client): T
    }
}