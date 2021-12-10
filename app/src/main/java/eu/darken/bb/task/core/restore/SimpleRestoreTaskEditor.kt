package eu.darken.bb.task.core.restore

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.backup.core.*
import eu.darken.bb.backup.core.app.AppRestoreConfig
import eu.darken.bb.backup.core.files.FilesBackupSpec
import eu.darken.bb.backup.core.files.FilesRestoreConfig
import eu.darken.bb.common.coroutine.AppScope
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.error.getRootCause
import eu.darken.bb.common.files.core.APath
import eu.darken.bb.common.files.core.GatewaySwitch
import eu.darken.bb.common.flow.DynamicStateFlow
import eu.darken.bb.common.flow.replayingShare
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.backupInfosOpt
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.TaskEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class SimpleRestoreTaskEditor @AssistedInject constructor(
    @Assisted private val taskId: Task.Id,
    private val restoreConfigRepo: RestoreConfigRepo,
    private val storageManager: StorageManager,
    private val pathTool: GatewaySwitch,
    @AppScope private val appScope: CoroutineScope,
) : TaskEditor {

    private val editorDataPub = DynamicStateFlow(TAG, appScope) { Data(taskId = taskId) }
    override val editorData = editorDataPub.flow

    private val backupInfoCache = mutableMapOf<Backup.Id, Backup.InfoOpt>()
    val backupInfos: Flow<List<Backup.InfoOpt>> = editorData.map { it.backupTargets }
        .distinctUntilChanged()
        .flatMapLatest { targets ->
            if (targets.isEmpty()) return@flatMapLatest flowOf(emptyList<Backup.InfoOpt>())
            // TODO make lookup more efficient, e.g. group by storage id.
            val obs = targets.map { target ->
                val cachedInfo = backupInfoCache[target.backupId]
                if (cachedInfo != null) {
                    flowOf(cachedInfo)
                } else {
                    val storage = storageManager.getStorage(target.storageId)
                    storage.backupInfosOpt(
                        Pair(target.backupSpecId, target.backupId),
                        live = false
                    )
                        .map { it.first() }
                        .onEach { backupInfoCache[it.backupId] = it }
                }
            }
            combine(obs) {
                it.asList()
            }
        }
        .replayingShare(appScope)


    private suspend fun Backup.Target.createConfigWrap(
        defaultConfig: Restore.Config,
        customConfig: Restore.Config?,
    ): ConfigWrap {
        val target = this
        val config = customConfig ?: defaultConfig

        val infos = backupInfos.first()
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
    val configWraps: Flow<List<ConfigWrap>> = combine(backupInfos, editorData) { infos, data ->
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
        .catch {
            if (it is InterruptedException || it.getRootCause() is InterruptedException) {
                log(TAG) { "configWrap: Ignoring interrupt exception" }
            } else {
                throw it
            }
        }
        .replayingShare(appScope)

    override suspend fun load(task: Task) {
        require(taskId == task.taskId) { "IDs don't match" }
        task as SimpleRestoreTask

        editorDataPub.updateBlocking {
            copy(
                label = task.label,
                isExistingTask = true,
                backupTargets = task.backupTargets,
                defaultConfigs = task.defaultConfigs,
                customConfigs = task.customConfigs
            )
        }
    }

    override suspend fun snapshot(): Task {
        val data = editorDataPub.value()

        // TODO test clean up custom configs
        val nonDefaultConfigs = data.customConfigs
            .filterNot { data.defaultConfigs.values.contains(it.value) }

        var label = data.label
        if (label.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy.MM.dd hh:mm", Locale.getDefault())
            label = sdf.format(Date())
        }

        return SimpleRestoreTask(
            taskId = data.taskId,
            label = label,
            defaultConfigs = data.defaultConfigs,
            customConfigs = nonDefaultConfigs,
            backupTargets = data.backupTargets,
            isSingleUse = data.isSingleUse
        )
    }

    override fun isValid(): Flow<Boolean> = combine(configWraps, editorData) { configWrappers, editorData ->
        val noMissingPermission = configWrappers.find {
            it is FilesConfigWrap && !it.isPermissionGranted
        } == null
        return@combine noMissingPermission
    }

    override suspend fun updateLabel(label: String) {
        editorDataPub.updateBlocking {
            copy(label = label)
        }
    }

    suspend fun updatePath(backupId: Backup.Id, newPath: APath) = updateCustomConfig(backupId) {
        it as FilesRestoreConfig
        it.copy(restorePath = newPath)
    }

    suspend fun excludeBackup(excludedId: Backup.Id) {
        editorDataPub.updateBlocking {
            val removed = backupTargets.single { it.backupId == excludedId }
            val newTargets = backupTargets.filter { it.backupId != excludedId }.toSet()

            val newDefaults = if (newTargets.none { it.backupType == removed.backupType }) {
                defaultConfigs.filter { it.value.restoreType != removed.backupType }
            } else {
                defaultConfigs
            }

            copy(
                backupTargets = newTargets,
                defaultConfigs = newDefaults
            )
        }
    }

    suspend fun updateDefaultConfig(config: Restore.Config): Map<Backup.Type, Restore.Config> =
        editorDataPub.updateBlocking {
            val newConfigs = defaultConfigs.toMutableMap()
            Timber.tag(TAG).d("Replacing default generator %s with %s", newConfigs[config.restoreType], config)
            newConfigs[config.restoreType] = config
            copy(defaultConfigs = newConfigs)
        }.defaultConfigs

    suspend fun setSingleUse(isSingleUse: Boolean): Data = editorDataPub.updateBlocking {
        copy(isSingleUse = isSingleUse)
    }


    suspend fun updateCustomConfig(
        backupId: Backup.Id,
        updateAction: (Restore.Config?) -> Restore.Config
    ): Map<Backup.Id, Restore.Config> = editorDataPub.updateBlocking {
        val configs = customConfigs.toMutableMap()
        val type = backupTargets.single { it.backupId == backupId }.backupType
        val oldConfig = configs[backupId] ?: defaultConfigs[type]!!
        val newConfig = updateAction(oldConfig)
        Timber.tag(TAG).d("Replacing custom generator %s with %s", oldConfig, newConfig)
        configs[backupId] = newConfig
        copy(customConfigs = configs)
    }.customConfigs

    private suspend fun addTargets(vararg targets: Backup.Target) {
        val defaults = restoreConfigRepo.getDefaultConfigs()
        val wanted = targets.map { it.backupType }
        val newConfigs = defaults.filter { wanted.contains(it.restoreType) }

        editorDataPub.updateBlocking {
            val existing = defaultConfigs.toMutableMap()
            newConfigs.forEach {
                if (existing[it.restoreType] == null) {
                    existing[it.restoreType] = it
                }
            }
            copy(
                backupTargets = backupTargets.plus(targets),
                defaultConfigs = existing.toMap()
            )
        }
    }

    suspend fun addStorageId(storageId: Storage.Id): Collection<Backup.Target> {
        val storage = storageManager.getStorage(storageId)
        val infos = storage.specInfos().first()
        val targets = infos
            .filter {
                val isEmpty = it.backups.isEmpty()
                if (isEmpty) Timber.tag(TAG).d("Empty spec: %s", it)
                !isEmpty
            }
            .map {
                val newest = it.backups.getNewest()!!
                Backup.Target(storageId, it.specId, newest.backupId, newest.backupType)
            }

        addTargets(*targets.toTypedArray())

        return targets
    }

    suspend fun addBackupSpecId(storageId: Storage.Id, backupSpecId: BackupSpec.Id): Backup.Target {
        val storage = storageManager.getStorage(storageId)
        val infos = storage.specInfo(backupSpecId).first()
        require(infos.backups.isNotEmpty()) { "BackupSpec contains no backups." }

        val newest = infos.backups.getNewest()!!
        val target = Backup.Target(storageId, infos.specId, newest.backupId, newest.backupType)
        addTargets(target)

        return target
    }

    suspend fun addBackupId(
        storageId: Storage.Id,
        backupSpecId: BackupSpec.Id,
        backupId: Backup.Id,
        backupType: Backup.Type
    ): Backup.Target {
        val target = Backup.Target(storageId, backupSpecId, backupId, backupType)
        addTargets(target)
        return target
    }

    data class Data(
        override val taskId: Task.Id,
        override val label: String = "",
        override val isExistingTask: Boolean = false,
        override val isSingleUse: Boolean = false,
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