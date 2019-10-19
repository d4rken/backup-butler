package eu.darken.bb.task.core.restore

import com.jakewharton.rx.replayingShare
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.*
import eu.darken.bb.common.HotData
import eu.darken.bb.common.rx.filterUnchanged
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.backupInfosOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
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
            .filterUnchanged()
            .switchMap { targets ->
                // TODO make lookup more efficient, e.g. group by storage id.
                val obs = targets.map { target ->
                    storageManager.getStorage(target.storageId)
                            .subscribeOn(Schedulers.io())
                            .switchMap { it.backupInfosOpt(Pair(target.backupSpecId, target.backupId), live = false) }
                            .map { it.first() }
                }
                Observable.combineLatest(obs) { it.asList() as List<Backup.InfoOpt> }
            }
            .replayingShare()

    val customConfigs: Observable<List<CustomConfigWrap>> = Observables
            .combineLatest(backupInfos, editorData)
            .map { (infos, data) ->
                infos.map { infoOpt ->
                    val type = data.backupTargets.single { it.backupId == infoOpt.backupId }.backupType
                    var config = data.customConfigs[infoOpt.backupId]
                    val isCustom = config != null
                    if (config == null) config = data.defaultConfigs.getValue(type)
                    CustomConfigWrap(
                            backupInfo = infoOpt,
                            config = config,
                            isCustomConfig = isCustom
                    )
                }
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

        // TODO test clean up custom configs
        val nonDefaultConfigs = data.customConfigs
                .filterNot { data.defaultConfigs.values.contains(it.value) }

        SimpleRestoreTask(
                taskId = data.taskId,
                label = data.label,
                defaultConfigs = data.defaultConfigs,
                customConfigs = nonDefaultConfigs,
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

    fun updateDefaultConfig(config: Restore.Config): Single<Map<Backup.Type, Restore.Config>> = editorDataPub
            .updateRx { data ->
                val newConfigs = data.defaultConfigs.toMutableMap()
                Timber.tag(TAG).d("Replacing default config %s with %s", newConfigs[config.restoreType], config)
                newConfigs[config.restoreType] = config
                data.copy(defaultConfigs = newConfigs)

            }
            .map { it.newValue.defaultConfigs }

    fun updateCustomConfig(backupId: Backup.Id, config: Restore.Config): Single<Map<Backup.Id, Restore.Config>> = editorDataPub
            .updateRx { data ->
                val newConfigs = data.customConfigs.toMutableMap()
                Timber.tag(TAG).d("Replacing custom config %s with %s", newConfigs[backupId], config)
                // TODO test Restore.Config equals/hash
                newConfigs[backupId] = config
                data.copy(customConfigs = newConfigs)

            }
            .map { it.newValue.customConfigs }

    private fun addTargets(vararg targets: Backup.Target): Completable = restoreConfigRepo.getDefaultConfigs()
            .map { defaults ->
                val wanted = targets.map { it.backupType }
                defaults.filter { wanted.contains(it.restoreType) }
            }
            .flatMap { newConfigs ->
                editorDataPub.updateRx { data ->
                    val existing = data.defaultConfigs.toMutableMap()
                    newConfigs.forEach {
                        if (existing[it.restoreType] == null) {
                            existing[it.restoreType] = it
                        }
                    }
                    data.copy(
                            backupTargets = data.backupTargets.plus(targets),
                            defaultConfigs = existing.toMap()
                    )
                }
            }
            .ignoreElement()


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
                Backup.Target(storageId, it.specId, newest.backupId, newest.backupType)
            }
            .toList()
            .flatMap { addTargets(*it.toTypedArray()).toSingleDefault(it) }

    fun addBackupSpecId(storageId: Storage.Id, backupSpecId: BackupSpec.Id): Single<Backup.Target> = storageManager.getStorage(storageId)
            .firstOrError()
            .flatMap { it.specInfo(backupSpecId).firstOrError() }
            .doOnSuccess { require(it.backups.isNotEmpty()) { "BackupSpec contains no backups." } }
            .map {
                val newest = it.backups.getNewest()!!
                Backup.Target(storageId, it.specId, newest.backupId, newest.backupType)
            }
            .flatMap { addTargets(it).toSingleDefault(it) }

    fun addBackupId(storageId: Storage.Id, backupSpecId: BackupSpec.Id, backupId: Backup.Id, backupType: Backup.Type): Single<Backup.Target> = Single
            .just(Backup.Target(storageId, backupSpecId, backupId, backupType))
            .flatMap { addTargets(it).toSingleDefault(it) }

    data class Data(
            override val taskId: Task.Id,
            override val label: String = "",
            override val existingTask: Boolean = false,
            val customConfigs: Map<Backup.Id, Restore.Config> = emptyMap(),
            val defaultConfigs: Map<Backup.Type, Restore.Config> = emptyMap(),
            val backupTargets: Set<Backup.Target> = emptySet()
    ) : TaskEditor.Data

    data class CustomConfigWrap(
            val backupInfo: Backup.InfoOpt,
            val config: Restore.Config,
            val isCustomConfig: Boolean
    )

    companion object {
        internal val TAG = App.logTag("Task", "Restore", "Editor", "Simple")
    }

    @AssistedInject.Factory
    interface Factory : TaskEditor.Factory<SimpleRestoreTaskEditor>

}