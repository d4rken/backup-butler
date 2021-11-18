package eu.darken.bb.processor.core.processors.restore

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.RestoreConfigRepo
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.debug.logging.logTag
import eu.darken.bb.common.flow.launchForAction
import eu.darken.bb.common.progress.*
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.ProcessorScope
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.restore.SimpleRestoreTask
import eu.darken.bb.task.core.results.SimpleResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Provider

class SimpleRestoreProcessor @AssistedInject constructor(
    @Assisted progressParent: Progress.Client,
    @ApplicationContext context: Context,
    private val restoreEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Provider<Restore.Endpoint>>,
    private val restoreConfigRepo: RestoreConfigRepo,
    private val storageManager: StorageManager,
    private val mmDataRepo: MMDataRepo,
    @ProcessorScope private val processorScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : SimpleBaseProcessor(context, progressParent, processorScope, dispatcherProvider) {

    override fun updateProgress(update: suspend Progress.Data.() -> Progress.Data) = progressParent.updateProgress(
        update
    )

    override suspend fun doProcess(task: Task) {
        progressParent.updateProgressPrimary(task.taskType.labelRes)
        progressParent.updateProgressSecondary(task.label)

        task as SimpleRestoreTask
        progressParent.updateProgressCount(Progress.Count.Percent(0, task.backupTargets.size))

        val endpointCache = mutableMapOf<Backup.Type, Restore.Endpoint>()

        // Most specific first
        task.backupTargets.forEachIndexed { index, target ->
            Timber.tag(TAG).v("Restoring %s", target)
            progressParent.updateProgressTertiary(R.string.progress_restoring_backup)

            val subResultBuilder = SimpleResult.SubResult.Builder()

            try {
                restoreBackup(task, endpointCache, target, subResultBuilder)
                subResultBuilder.sucessful()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error while processing backup target: %s", target)
                subResultBuilder.error(context, e)
            } finally {
                resultBuilder.addSubResult(subResultBuilder)
            }

            progressParent.updateProgressCount(Progress.Count.Percent(index + 1, task.backupTargets.size))
        }
    }

    private suspend fun getConfig(task: SimpleRestoreTask, metaData: Backup.MetaData): Restore.Config {
        var config = task.customConfigs[metaData.backupId]
        if (config == null) {
            config = task.defaultConfigs[metaData.backupType]
        }
        if (config == null) {
            val defaults = restoreConfigRepo.getDefaultConfigs()
            config = defaults.single { it.restoreType == metaData.backupType }
        }
        return config
    }

    private suspend fun restoreBackup(
        task: SimpleRestoreTask,
        endpointCache: MutableMap<Backup.Type, Restore.Endpoint>,
        target: Backup.Target,
        subResultBuilder: SimpleResult.SubResult.Builder
    ) {
        subResultBuilder.label(target.toString()) // If there are errors before getting a better label

        val storage = storageManager.getStorage(target.storageId)
        storage.keepAliveWith(this)

        val specInfo = storage.specInfo(target.backupSpecId).first()
        subResultBuilder.label(specInfo.backupSpec.getLabel(context))

        progressParent.updateProgressTertiary { specInfo.backupSpec.getLabel(it) }
        val backupMeta = specInfo.backups.find { it.backupId == target.backupId }!!
        val config = getConfig(task, backupMeta)

        val backupType = backupMeta.backupType
        val backupId = backupMeta.backupId

        Timber.tag(TAG).d("Loading backup unit from storage %s", storage)
        val backupUnit = storage.forwardProgressTo(progressChild).launchForAction(processorScope) {
            storage.load(specInfo.specId, backupId)
        }
        Timber.tag(TAG).d("Backup unit loaded: %s", backupUnit)

        val endpoint = endpointCache.getOrPut(backupType) {
            restoreEndpointFactories.getValue(backupType).get().keepAliveWith(this)
        }

        Timber.tag(TAG).d("Restoring %s with endpoint %s", backupUnit.spec, endpoint)

        endpoint.forwardProgressTo(progressChild).launchForAction(processorScope) {
            endpoint.sharedResource.get().use {
                endpoint.restore(config, backupUnit) {
                    subResultBuilder.addLogEvent(it)
                }
            }
        }

        Timber.tag(TAG).d("Restoration done for %s", backupUnit.spec)

        mmDataRepo.release(backupUnit.backupId)
    }

    @AssistedFactory
    interface Factory : Processor.Factory<SimpleRestoreProcessor>

    companion object {
        private val TAG = logTag("Processor", "Restore", "Simple")
    }
}