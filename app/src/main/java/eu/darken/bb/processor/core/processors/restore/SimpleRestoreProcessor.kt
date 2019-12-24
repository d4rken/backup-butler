package eu.darken.bb.processor.core.processors.restore

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.backup.core.RestoreConfigRepo
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.*
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.restore.SimpleRestoreTask
import eu.darken.bb.task.core.results.SimpleResult
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Provider

class SimpleRestoreProcessor @AssistedInject constructor(
        @Assisted progressParent: Progress.Client,
        @AppContext context: Context,
        private val restoreEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Provider<Restore.Endpoint>>,
        private val restoreConfigRepo: RestoreConfigRepo,
        private val storageManager: StorageManager,
        private val mmDataRepo: MMDataRepo
) : SimpleBaseProcessor(context, progressParent) {

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressParent.updateProgress(update)

    override fun doProcess(task: Task) {
        progressParent.updateProgressPrimary(task.taskType.labelRes)
        progressParent.updateProgressSecondary(task.label)

        task as SimpleRestoreTask
        progressParent.updateProgressCount(Progress.Count.Counter(0, task.backupTargets.size))

        // Most specific first
        task.backupTargets.forEachIndexed { index, target ->
            progressParent.updateProgressTertiary(R.string.progress_restoring_backup)

            val subResultBuilder = SimpleResult.SubResult.Builder()

            try {
                restoreBackup(task, target, subResultBuilder)
                subResultBuilder.sucessful()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error while processing backup target: %s", target)
                subResultBuilder.error(context, e)
            } finally {
                resultBuilder.addSubResult(subResultBuilder)
            }

            progressParent.updateProgressCount(Progress.Count.Counter(index + 1, task.backupTargets.size))
        }
    }


    private fun getConfig(task: SimpleRestoreTask, metaData: Backup.MetaData): Restore.Config {
        var config = task.customConfigs[metaData.backupId]
        if (config == null) {
            config = task.defaultConfigs[metaData.backupType]
        }
        if (config == null) {
            val defaults = restoreConfigRepo.getDefaultConfigs().blockingGet()
            config = defaults.single { it.restoreType == metaData.backupType }
        }
        return config
    }

    private fun restoreBackup(
            task: SimpleRestoreTask,
            target: Backup.Target,
            subResultBuilder: SimpleResult.SubResult.Builder
    ) {
        subResultBuilder.label(target.toString()) // If there are errors before getting a better label

        val storage = storageManager.getStorage(target.storageId).blockingFirst()
        val specInfo = storage.specInfo(target.backupSpecId).blockingFirst()
        subResultBuilder.label(specInfo.backupSpec.getLabel(context))

        progressParent.updateProgressTertiary { it.getString(R.string.progress_processing_x_label, specInfo.backupSpec.getLabel(it)) }
        val backupMeta = specInfo.backups.find { it.backupId == target.backupId }!!
        val config = getConfig(task, backupMeta)

        val backupType = backupMeta.backupType
        val backupId = backupMeta.backupId

        val storageProgressSub = storage.progress
                .subscribeOn(Schedulers.io())
                .subscribe { pro -> progressChild.updateProgress { pro } }

        val backupUnit = storage.load(specInfo.specId, backupId)
        storageProgressSub.dispose()

        val endpointFactory = restoreEndpointFactories[backupType]
        requireNotNull(endpointFactory) { "Unknown endpoint: type=$backupType ($specInfo" }

        endpointFactory.get().use { endpoint ->

            val endpointProgressSub = endpoint.progress
                    .subscribeOn(Schedulers.io())
                    .subscribe { pro -> progressChild.updateProgress { pro } }

            endpoint.restore(config, backupUnit) {
                subResultBuilder.addLogEvent(it)
            }

            endpointProgressSub.dispose()
        }

        mmDataRepo.release(backupUnit.backupId)
    }

    @AssistedInject.Factory
    interface Factory : Processor.Factory<SimpleRestoreProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Restore", "Simple")
    }
}