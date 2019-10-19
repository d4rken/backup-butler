package eu.darken.bb.task.core.restore

import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.*
import eu.darken.bb.common.HotData
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.backupInfosOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class SimpleRestoreTaskEditor @AssistedInject constructor(
        @Assisted private val taskId: Task.Id,
        private val restoreConfigRepo: RestoreConfigRepo,
        private val storageManager: StorageManager
) : TaskEditor {

    private val editorDataPub = HotData(Data(taskId = taskId, label = "RestoreTask"))
    override val editorData = editorDataPub.data

    val backupInfos: Observable<List<Backup.InfoOpt>> = editorData.map { it.backupTargets }
            .switchMap { targets ->
                // TODO make lookup more efficient, e.g. group by storage id.
                val obs = targets.map { target ->
                    storageManager.getStorage(target.storageId)
                            .subscribeOn(Schedulers.io())
                            .switchMap { it.backupInfosOpt(Pair(target.backupSpecId, target.backupId)) }
                            .map { it.first() }
                }
                return@switchMap Observable.combineLatest(obs) { it.asList() as List<Backup.InfoOpt> }
            }
            .replayingShare()

    override fun load(task: Task): Completable = Single.just(task as SimpleRestoreTask)
            .flatMap { simpleTask ->
                editorDataPub.updateRx {
                    it.copy(
                            label = task.label,
                            existingTask = true,
                            backupTargets = simpleTask.backupTargets,
                            defaultConfigs = simpleTask.defaultConfigs,
                            customConfigs = simpleTask.customConfigs
                    )
                }
            }
            .ignoreElement()

    override fun save(): Single<out Task> = Single.fromCallable {
        val data = editorDataPub.snapshot

        SimpleRestoreTask(
                taskId = data.taskId,
                label = data.label,
                defaultConfigs = data.defaultConfigs,
                customConfigs = data.customConfigs,
                backupTargets = data.backupTargets
        )
    }

    override fun isValidTask(): Observable<Boolean> = editorData.map { task ->
        task.label.isNotBlank()
    }

    override fun updateLabel(label: String) {
        editorDataPub.update {
            it.copy(label = label)
        }
    }

    fun updateConfig(backupId: Backup.Id, config: Restore.Config) {
        editorDataPub.update { old ->
            val newConfigs = old.customConfigs.toMutableMap()
            Timber.tag(TAG).d("Replacing config %s with %s", newConfigs[backupId], config)
            newConfigs[backupId] = config
            old.copy(customConfigs = newConfigs)
        }
    }

    fun addStorageId(storageId: Storage.Id): Single<Collection<Backup.Target>> = storageManager.getStorage(storageId)
            .take(1)
            .flatMap { it.specInfos().take(1) }
            .flatMapIterable { it }
            .filter {
                it.backups.isNotEmpty().also { notEmpty ->
                    if (!notEmpty) Timber.tag(TAG).d("Empty spec: %s", notEmpty)
                }
            }
            .map {
                val newest = it.backups.getNewest()!!
                Backup.Target(storageId, it.specId, newest.backupId)
            }
            .toList()
            .flatMap { targets ->
                editorDataPub
                        .updateRx { it.copy(backupTargets = it.backupTargets.plus(targets)) }
                        .map { targets }
            }

    fun addBackupSpecId(storageId: Storage.Id, backupSpecId: BackupSpec.Id): Single<Backup.Target> = storageManager.getStorage(storageId)
            .firstOrError()
            .flatMap { it.specInfo(backupSpecId).firstOrError() }
            .doOnSuccess { require(it.backups.isNotEmpty()) { "BackupSpec contains no backups." } }
            .map {
                val newest = it.backups.getNewest()!!
                Backup.Target(storageId, it.specId, newest.backupId)
            }
            .flatMap { target ->
                editorDataPub
                        .updateRx { it.copy(backupTargets = it.backupTargets.plus(target)) }
                        .map { target }
            }

    fun addBackupId(storageId: Storage.Id, backupSpecId: BackupSpec.Id, backupId: Backup.Id): Single<Backup.Target> = Single
            .just(Backup.Target(storageId, backupSpecId, backupId))
            .flatMap { target ->
                editorDataPub
                        .updateRx { it.copy(backupTargets = it.backupTargets.plus(target)) }
                        .map { target }
            }

    data class Data(
            override val taskId: Task.Id,
            override val label: String = "",
            override val existingTask: Boolean = false,
            val customConfigs: Map<Backup.Id, Restore.Config> = emptyMap(),
            val defaultConfigs: Map<Backup.Type, Restore.Config> = emptyMap(),
            val backupTargets: Set<Backup.Target> = emptySet()
    ) : TaskEditor.Data

    companion object {
        internal val TAG = App.logTag("Task", "Restore", "Editor", "Simple")
    }

    @AssistedInject.Factory
    interface Factory : TaskEditor.Factory<SimpleRestoreTaskEditor>
}