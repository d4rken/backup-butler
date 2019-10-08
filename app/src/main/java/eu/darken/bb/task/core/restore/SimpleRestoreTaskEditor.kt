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
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*


class SimpleRestoreTaskEditor @AssistedInject constructor(
        @Assisted private val taskId: Task.Id,
        private val restoreConfigRepo: RestoreConfigRepo
) : TaskEditor {

    private val configPub = HotData {
        val defaultConfigs = restoreConfigRepo.getDefaultConfigs().blockingGet()
        SimpleRestoreTask(
                taskId = taskId,
                taskName = UUID.randomUUID().toString().substring(0, 8),
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

    override fun isValidTask(): Observable<Boolean> = config.map { task ->
        task.taskName.isNotBlank()
    }

    override fun updateLabel(label: String) {
        configPub.update {
            it.copy(taskName = label)
        }
    }

    fun addStorageId(storageId: Storage.Id) {
        configPub.update { old ->
            old.copy(targetStorages = old.targetStorages.toMutableSet().apply { add(storageId) }.toSet())
        }
    }

    fun addBackupSpecId(storageId: Storage.Id, backupSpecId: BackupSpec.Id) {
        val target = BackupSpec.Target(storageId, backupSpecId)
        configPub.update { old ->
            old.copy(targetBackupSpec = old.targetBackupSpec.toMutableSet().apply { add(target) }.toSet())
        }
    }

    fun addBackupId(storageId: Storage.Id, backupSpecId: BackupSpec.Id, backupId: Backup.Id) {
        val target = Backup.Target(storageId, backupSpecId, backupId)
        configPub.update { old ->
            old.copy(targetBackup = old.targetBackup.toMutableSet().apply { add(target) }.toSet())
        }
    }

    fun updateConfig(config: Restore.Config) {
        configPub.update { old ->
            old.copy(restoreConfigs = old.restoreConfigs.replace(config, { it.restoreType == config.restoreType }).toSet())
        }
    }


    @AssistedInject.Factory
    interface Factory : TaskEditor.Factory<SimpleRestoreTaskEditor>
}