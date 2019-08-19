package eu.darken.bb.task.core.restore

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.RestoreConfigRepo
import eu.darken.bb.common.HotData
import eu.darken.bb.common.replace
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import io.reactivex.Completable
import io.reactivex.Single


class SimpleRestoreTaskEditor @AssistedInject constructor(
        @Assisted private val taskId: Task.Id,
        private val restoreConfigRepo: RestoreConfigRepo
) : TaskEditor {

    private val configPub = HotData {
        val defaultConfigs = restoreConfigRepo.getDefaultConfigs().blockingGet()
        SimpleRestoreTask(
                taskId = taskId,
                taskName = "",
                restoreConfigs = defaultConfigs
        )
    }
    override val config = configPub.data

    var isExisting = false

    override fun load(task: Task): Completable = Completable.fromCallable {
        isExisting = true
        task as SimpleRestoreTask
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

    fun addStorageId(storageId: Storage.Id) {
        configPub.update { old ->
            old.copy(storageIds = old.storageIds.toMutableSet().apply { add(storageId) }.toSet())
        }
    }

    fun addBackupSpecId(backupSpecId: BackupSpec.Id) {
        configPub.update { old ->
            old.copy(backupSpecIds = old.backupSpecIds.toMutableSet().apply { add(backupSpecId) }.toSet())
        }
    }

    fun addBackupId(backupId: Backup.Id) {
        configPub.update { old ->
            old.copy(backupIds = old.backupIds.toMutableSet().apply { add(backupId) }.toSet())
        }
    }

    fun updateConfig(config: Restore.Config) {
        configPub.update { old ->
            old.copy(restoreConfigs = old.restoreConfigs.replace(config, { it.restoreType == config.restoreType }))
        }
    }


    @AssistedInject.Factory
    interface Factory : TaskEditor.Factory<SimpleRestoreTaskEditor>
}