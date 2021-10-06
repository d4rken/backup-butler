package eu.darken.bb.task.core.restore

import com.jakewharton.rx3.replayingShare
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.backup.core.*
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.HotData
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.rx.filterUnchanged
import eu.darken.bb.common.rx.latest
import eu.darken.bb.common.rx.swallowInterruptExceptions
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.backupInfosOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.Observables
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class SimpleRestoreTaskEditor @AssistedInject constructor(
    @Assisted private val taskId: Task.Id,
    private val restoreConfigRepo: RestoreConfigRepo,
    private val storageManager: StorageManager,
    private val pathTool: GatewaySwitch
) : TaskEditor {

    private val editorDataPub = HotData(tag = TAG) { Data(taskId = taskId) }
    override val editorData = editorDataPub.data

    private val backupInfoCache = mutableMapOf<Backup.Id, Backup.InfoOpt>()
    val backupInfos: Observable<List<Backup.InfoOpt>> = editorData.map { it.backupTargets }
        .filterUnchanged()
        .switchMap { targets ->
            if (targets.isEmpty()) return@switchMap Observable.just(emptyList<Backup.InfoOpt>())
            // TODO make lookup more efficient, e.g. group by storage id.
            val obs = targets.map { target ->
                val cachedInfo = backupInfoCache[target.backupId]
                if (cachedInfo != null) {
                    Observable.just(cachedInfo)
                } else {
                    storageManager.getStorage(target.storageId)
                        .flatMapObservable {
                            it.backupInfosOpt(
                                Pair(target.backupSpecId, target.backupId),
                                live = false
                            )
                        }
                        .map { it.first() }
                        .doOnNext { backupInfoCache[it.backupId] = it }
                }
            }
            Observable.combineLatest(obs) { it.asList() as List<Backup.InfoOpt> }
        }
        .replayingShare()


    private fun Backup.Target.createConfigWrap(
        defaultConfig: Restore.Config,
        customConfig: Restore.Config?,
    ): ConfigWrap {
        val target = this
        val config = customConfig ?: defaultConfig

        val infos = backupInfos.blockingFirst()
        // Due to exclusion and data refresh, infos may no longer contains a data object
        val infoOpt = infos.find { it.backupId == target.backupId }

        return when (target.backupType) {
            Backup.Type.APP -> {
                config as AppRestoreConfig
                val isCustom = config != defaultConfig
                AppsConfigWrap(
                    backupInfoOpt = infoOpt,
                    config = config,
                    isCustomConfig = isCustom
                )
            }
            Backup.Type.FILES -> {
                config as FilesRestoreConfig
                defaultConfig as FilesRestoreConfig
                val defaultPath = (infoOpt?.info?.spec as? FilesBackupSpec)?.path
                val granted = (config.restorePath ?: defaultPath)?.let { pathTool.canWrite(it) } ?: false
                val isCustom = config != defaultConfig
                FilesConfigWrap(
                    backupInfoOpt = infoOpt,
                    config = config,
                    isCustomConfig = isCustom,
                    defaultPath = defaultPath,
                    isPermissionGranted = granted
                )
            }
        }
    }

    //    private val configWrapCache = mutableMapOf<Backup.Id, ConfigWrap>()
    val configWraps: Observable<List<ConfigWrap>> = Observables
        .combineLatest(backupInfos, editorData)
        .serialize()
        .map { (infos, data) ->
            data.backupTargets.map { target ->
//                val currentInfos = infos.find { it.backupId == target.backupId }
//                val currentWrap = configWrapCache[target.backupId]
//
//                // If the info changed, we need to update the wrap
//                val hashMissMatch = currentWrap?.backupInfoOpt?.hashCode() != currentInfos?.hashCode()

                val customConfig = data.customConfigs[target.backupId]
                val defaultConfig = data.defaultConfigs.getValue(target.backupType)

//                // if the config has changed, we need to update the wrap too
//                val newConfig = currentWrap?.config != customConfig || currentWrap?.config != defaultConfig
//
//                if (currentWrap == null || hashMissMatch || newConfig) {
//                    currentWrap = target.createConfigWrap(defaultConfig = defaultConfig, customConfig = customConfig)
//                    configWrapCache[target.backupId] = currentWrap
//                }
//                currentWrap
                target.createConfigWrap(defaultConfig = defaultConfig, customConfig = customConfig)
            }
        }
        .swallowInterruptExceptions()
        .replayingShare()

    override fun load(task: Task): Completable = Single.just(task as SimpleRestoreTask)
        .flatMap { simpleTask ->
            require(taskId == simpleTask.taskId) { "IDs don't match" }
            editorDataPub.updateRx {
                it.copy(
                    label = task.label,
                    isExistingTask = true,
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

        var label = data.label
        if (label.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy.MM.dd hh:mm", Locale.getDefault())
            label = sdf.format(Date())
        }

        SimpleRestoreTask(
            taskId = data.taskId,
            label = label,
            defaultConfigs = data.defaultConfigs,
            customConfigs = nonDefaultConfigs,
            backupTargets = data.backupTargets,
            isOneTimeTask = data.isOneTimeTask
        )
    }

    override fun isValid(): Observable<Boolean> = Observables.combineLatest(configWraps, editorData)
        .map { (configWrappers, editorData) ->
            val noMissingPermission = configWrappers.find {
                it is FilesConfigWrap && !it.isPermissionGranted
            } == null
            return@map noMissingPermission
        }

    override fun updateLabel(label: String) {
        editorDataPub.update {
            it.copy(label = label)
        }
    }

    fun updatePath(backupId: Backup.Id, newPath: APath) = updateCustomConfig(backupId) {
        it as FilesRestoreConfig
        it.copy(restorePath = newPath)
    }

    fun excludeBackup(excludedId: Backup.Id) {
        editorDataPub.update { data ->
            val removed = data.backupTargets.single { it.backupId == excludedId }
            val newTargets = data.backupTargets.filter { it.backupId != excludedId }.toSet()

            val newDefaults = if (newTargets.none { it.backupType == removed.backupType }) {
                data.defaultConfigs.filter { it.value.restoreType != removed.backupType }
            } else {
                data.defaultConfigs
            }

            data.copy(
                backupTargets = newTargets,
                defaultConfigs = newDefaults
            )
        }
    }

    fun updateDefaultConfig(config: Restore.Config): Single<Map<Backup.Type, Restore.Config>> = editorDataPub
        .updateRx { data ->
            val newConfigs = data.defaultConfigs.toMutableMap()
            Timber.tag(TAG).d("Replacing default generator %s with %s", newConfigs[config.restoreType], config)
            newConfigs[config.restoreType] = config
            data.copy(defaultConfigs = newConfigs)
        }
        .map { it.newValue.defaultConfigs }

    fun updateOneTime(isOneTimeTask: Boolean): Single<Data> = editorDataPub
        .updateRx {
            it.copy(isOneTimeTask = isOneTimeTask)
        }
        .map { it.newValue }


    fun updateCustomConfig(
        backupId: Backup.Id,
        updateAction: (Restore.Config?) -> Restore.Config
    ): Single<Map<Backup.Id, Restore.Config>> = editorDataPub
        .updateRx { data ->
            val configs = data.customConfigs.toMutableMap()
            val type = data.backupTargets.single { it.backupId == backupId }.backupType
            val oldConfig = configs[backupId] ?: data.defaultConfigs[type]!!
            val newConfig = updateAction(oldConfig)
            Timber.tag(TAG).d("Replacing custom generator %s with %s", oldConfig, newConfig)
            configs[backupId] = newConfig
            data.copy(customConfigs = configs)

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
        .flatMap { it.specInfos().latest() }
        .map { infos ->
            infos
                .filter {
                    val isEmpty = it.backups.isEmpty()
                    if (isEmpty) Timber.tag(TAG).d("Empty spec: %s", it)
                    !isEmpty
                }
                .map {
                    val newest = it.backups.getNewest()!!
                    Backup.Target(storageId, it.specId, newest.backupId, newest.backupType)
                }
        }
        .flatMap { targets -> addTargets(*targets.toTypedArray()).toSingleDefault(targets) }

    fun addBackupSpecId(storageId: Storage.Id, backupSpecId: BackupSpec.Id): Single<Backup.Target> =
        storageManager.getStorage(storageId)
            .flatMap { it.specInfo(backupSpecId).latest() }
            .doOnSuccess { require(it.backups.isNotEmpty()) { "BackupSpec contains no backups." } }
            .map {
                val newest = it.backups.getNewest()!!
                Backup.Target(storageId, it.specId, newest.backupId, newest.backupType)
            }
            .flatMap { addTargets(it).toSingleDefault(it) }

    fun addBackupId(
        storageId: Storage.Id,
        backupSpecId: BackupSpec.Id,
        backupId: Backup.Id,
        backupType: Backup.Type
    ): Single<Backup.Target> = Single
        .just(Backup.Target(storageId, backupSpecId, backupId, backupType))
        .flatMap { addTargets(it).toSingleDefault(it) }

    data class Data(
        override val taskId: Task.Id,
        override val label: String = "",
        override val isExistingTask: Boolean = false,
        override val isOneTimeTask: Boolean = false,
        val customConfigs: Map<Backup.Id, Restore.Config> = emptyMap(),
        val defaultConfigs: Map<Backup.Type, Restore.Config> = emptyMap(),
        val backupTargets: Set<Backup.Target> = emptySet()
    ) : TaskEditor.Data

    interface ConfigWrap {
        val config: Restore.Config
        val backupInfoOpt: Backup.InfoOpt?
        val isCustomConfig: Boolean
        val isValid: Boolean
    }

    data class FilesConfigWrap(
        override val config: FilesRestoreConfig,
        override val backupInfoOpt: Backup.InfoOpt? = null,
        override val isCustomConfig: Boolean = false,
        val isPermissionGranted: Boolean = backupInfoOpt == null,
        val defaultPath: APath? = null
    ) : ConfigWrap {

        override val isValid: Boolean
            get() = isPermissionGranted

        val currentPath: APath?
            get() = config.restorePath ?: defaultPath
    }

    data class AppsConfigWrap(
        override val config: AppRestoreConfig,
        override val backupInfoOpt: Backup.InfoOpt? = null,
        override val isCustomConfig: Boolean = false
    ) : ConfigWrap {
        override val isValid: Boolean
            get() = true
    }

    companion object {
        internal val TAG = logTag("Task", "Restore", "Editor", "Simple")
    }

    @AssistedFactory
    interface Factory : TaskEditor.Factory<SimpleRestoreTaskEditor>

}