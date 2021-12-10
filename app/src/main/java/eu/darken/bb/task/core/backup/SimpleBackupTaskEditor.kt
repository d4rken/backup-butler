package eu.darken.bb.task.core.backup

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.Logging.Priority.WARN
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SimpleBackupTaskEditor @AssistedInject constructor(
    @Assisted val taskId: Task.Id,
    @AppScope private val appScope: CoroutineScope,
) : TaskEditor {
    private val editorDataPub = DynamicStateFlow(TAG, appScope) { Data(taskId = taskId) }
    override val editorData = editorDataPub.flow

    override suspend fun load(task: Task) {
        task as SimpleBackupTask
        require(taskId == task.taskId) { "IDs don't match" }
        editorDataPub.updateBlocking {
            copy(
                label = task.label,
                isExistingTask = true,
                sources = task.sources,
                destinations = task.destinations
            )
        }
    }

    override suspend fun snapshot(): Task {
        val data = editorDataPub.value()
        return SimpleBackupTask(
            taskId = data.taskId,
            label = data.label,
            sources = data.sources,
            destinations = data.destinations,
            isSingleUse = data.isSingleUse
        )
    }

    override fun isValid(): Flow<Boolean> = editorData.map { data ->
        data.label.isNotBlank()
    }

    override suspend fun updateLabel(label: String) {
        editorDataPub.updateBlocking {
            copy(label = label)
        }
    }

    suspend fun addStorage(storageId: Storage.Id) {
        editorDataPub.updateBlocking {
            if (destinations.contains(storageId)) {
                log(TAG, WARN) { "Storage.Id was already added as storage: $storageId" }
            }
            copy(destinations = destinations.toMutableSet().apply { add(storageId) }.toSet())
        }
    }

    suspend fun removeStorage(storageId: Storage.Id) {
        editorDataPub.updateBlocking {
            if (!destinations.contains(storageId)) {
                log(TAG, WARN) { "Task ($taskId) does not contain storage $storageId" }
            }
            copy(destinations = destinations.toMutableSet().filterNot { it == storageId }.toSet())
        }
    }

    suspend fun addGenerator(generatorId: Generator.Id) {
        editorDataPub.updateBlocking {
            if (sources.contains(generatorId)) {
                log(TAG, WARN) { "Generator.Id was already added: $generatorId" }
            }
            copy(sources = sources.toMutableSet().apply { add(generatorId) }.toSet())
        }
    }

    suspend fun removeGenerator(generatorId: Generator.Id) {
        editorDataPub.updateBlocking {
            if (sources.contains(generatorId)) {
                log(TAG, WARN) { "Task ($taskId) does not contain generator: $generatorId" }
            }
            copy(sources = sources.toMutableSet().filterNot { it == generatorId }.toSet())
        }
    }

    suspend fun setSingleUse(isSingleUse: Boolean) {
        editorDataPub.updateBlocking {
            copy(isSingleUse = isSingleUse)
        }
    }

    data class Data(
        override val taskId: Task.Id,
        override val label: String = "",
        override val isExistingTask: Boolean = false,
        override val isSingleUse: Boolean = false,
        val sources: Set<Generator.Id> = emptySet(),
        val destinations: Set<Storage.Id> = emptySet()
    ) : TaskEditor.Data

    companion object {
        internal val TAG = logTag("Task", "Restore", "Editor", "Simple")
    }

    @AssistedFactory
    interface Factory : TaskEditor.Factory<SimpleBackupTaskEditor>
}