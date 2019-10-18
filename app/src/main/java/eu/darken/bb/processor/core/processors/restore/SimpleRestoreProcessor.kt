package eu.darken.bb.processor.core.processors.restore

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.*
import eu.darken.bb.common.OpStatus
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.restore.SimpleRestoreTask
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Provider

class SimpleRestoreProcessor @AssistedInject constructor(
        @Assisted progressParent: Progress.Client,
        @AppContext context: Context,
        private val restoreEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Provider<Restore.Endpoint>>,
        private val MMDataRepo: MMDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageRefRepo: StorageRefRepo,
        private val storageManager: StorageManager
) : SimpleBaseProcessor(context, progressParent) {


    override fun doProcess(task: Task) {
        task as SimpleRestoreTask

        var success = 0
        var skipped = 0
        var error = 0

        val alreadyRestored = mutableSetOf<Backup.Id>()

        // Most specific first
        task.targetBackup.forEach { backupTarget ->
            val storage = storageManager.getStorage(backupTarget.storageId).blockingFirst()
            val specInfo = storage.specInfo(backupTarget.backupSpecId).blockingFirst()
            val backupMeta = specInfo.backups.find { it.backupId == backupTarget.backupId }!!
            if (restoreBackup(storage, task.restoreConfigs, specInfo.specId, backupMeta)) {
                alreadyRestored.add(backupMeta.backupId)
            }
        }

        task.targetBackupSpec.forEach { specTarget ->
            val storage = storageManager.getStorage(specTarget.storageId).blockingFirst()
            val specInfo = storage.specInfo(specTarget.backupSpecId).blockingFirst()
            val newestBackup = specInfo.backups.getNewest()
            if (newestBackup != null) {
                if (restoreBackup(storage, task.restoreConfigs, specInfo.specId, newestBackup)) {
                    alreadyRestored.add(newestBackup.backupId)
                }
            } else {
                Timber.tag(TAG).d("Empty BackupSpec: %s", specInfo)
            }
        }

        task.targetStorages.forEach { storageId ->
            val storage = storageManager.getStorage(storageId).blockingFirst()
            storage.specInfos().take(1).flatMapIterable { it }.forEach { specItem ->
                val newest = specItem.backups.getNewest()
                if (newest != null) {
                    if (restoreBackup(storage, task.restoreConfigs, specItem.specId, newest)) {
                        alreadyRestored.add(newest.backupId)
                    }
                } else {
                    Timber.tag(TAG).d("Empty BackupSpec: %s", specItem.backupSpec)
                }
            }
        }

        resultBuilder.primary(
                OpStatus(context).apply {
                    this.success = success
                    this.skipped = skipped
                    this.failed = error
                }.toDisplayString()
        )
    }

    private fun restoreBackup(
            source: Storage,
            configs: Collection<Restore.Config>,
            specId: BackupSpec.Id,
            backupMetadata: Backup.MetaData
    ): Boolean {
        val backupType = backupMetadata.backupType
        val backupId = backupMetadata.backupId

        val storageProgressSub = source.progress
                .subscribeOn(Schedulers.io())
                .subscribe { pro -> progressChild.updateProgress { pro } }

        val backupUnit = source.load(specId, backupId)
        storageProgressSub.dispose()

        val endpointFactory = restoreEndpointFactories[backupType]
        requireNotNull(endpointFactory) { "Unknown endpoint: type=$backupType (${specId}" }
        val endpoint = endpointFactory.get()

        val endpointProgressSub = endpoint.progress
                .subscribeOn(Schedulers.io())
                .subscribe { pro -> progressChild.updateProgress { pro } }

        val config: Restore.Config = configs.find { it.restoreType == backupType }!!
        endpoint.restore(config, backupUnit)

        endpointProgressSub.dispose()

        // TODO success? true/false?
        return true
    }

    @AssistedInject.Factory
    interface Factory : Processor.Factory<SimpleRestoreProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Restore", "Simple")
    }
}