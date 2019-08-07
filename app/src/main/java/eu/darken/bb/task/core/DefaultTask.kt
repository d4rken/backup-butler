package eu.darken.bb.task.core

import android.content.Context
import eu.darken.bb.R
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.storage.core.Storage

data class DefaultTask(
        override val taskName: String,
        override val taskId: Task.Id,
        override val sources: Set<Generator.Id>,
        override val destinations: Set<Storage.Id>
) : Task {

    override fun getDescription(context: Context): String {
        return context.getString(R.string.default_backuptask_description_x_sources_x_destinations, sources.size, destinations.size)
    }

    data class Result(
            override val taskID: Task.Id,
            override val state: Task.Result.State,
            override val error: Exception? = null,
            override val primary: String? = null,
            override val secondary: String? = null
    ) : Task.Result {
        constructor(taskId: Task.Id, error: Exception)
                : this(taskID = taskId, state = Task.Result.State.ERROR, error = error)
    }

}