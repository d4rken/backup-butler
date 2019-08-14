package eu.darken.bb.task.core.backup

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.HotData
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.SimpleBackupTask
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import io.reactivex.Completable
import io.reactivex.Single


class SimpleBackupTaskEditor @AssistedInject constructor(
        @Assisted private val taskId: Task.Id
) : TaskEditor {

    private val configPub = HotData(SimpleBackupTask(
            taskId = taskId,
            taskName = "",
            sources = emptySet(),
            destinations = emptySet()
    ))
    override val config = configPub.data

    var isExisting = false

    override fun load(task: Task): Completable = Completable.fromCallable {
        isExisting = true
        task as SimpleBackupTask
        configPub.update { task }
    }

    override fun save(): Single<out Task> = configPub.data.firstOrError()

    override fun isExistingTask(): Boolean = isExisting

    override fun isValidTask(): Boolean = true

    override fun updateLabel(label: String) {
        configPub.update {
            it.copy(taskName = label)
        }
    }

    fun addDesination(storageId: Storage.Id) {
        configPub.update {
            it.copy(destinations = it.destinations.toMutableSet().apply { add(storageId) }.toSet())
        }
    }

    fun removeDesination(storageId: Storage.Id) {
        configPub.update { task ->
            task.copy(destinations = task.destinations.toMutableSet().filterNot { it == storageId }.toSet())
        }
    }

    fun addSource(generatorId: Generator.Id) {
        configPub.update {
            it.copy(sources = it.sources.toMutableSet().apply { add(generatorId) }.toSet())
        }
    }

    fun removeSource(generatorId: Generator.Id) {
        configPub.update { task ->
            task.copy(sources = task.sources.toMutableSet().filterNot { it == generatorId }.toSet())
        }
    }

    @AssistedInject.Factory
    interface Factory : TaskEditor.Factory<SimpleBackupTaskEditor>
}