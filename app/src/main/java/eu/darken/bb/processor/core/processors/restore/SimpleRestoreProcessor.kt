package eu.darken.bb.processor.core.processors.restore

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.BackupSpec
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.backup.core.Restore
import eu.darken.bb.common.OpStatus
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.restore.SimpleRestoreTask
import timber.log.Timber

class SimpleRestoreProcessor @AssistedInject constructor(
        @Assisted progressParent: Progress.Client,
        @AppContext context: Context,
        private val restoreEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Restore.Endpoint.Factory<out Restore.Endpoint>>,
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
        val dontOverlap = mutableSetOf<BackupSpec.Id>()

        // Most specific first
        task.targetBackup.forEach {
            TODO("Specific backup restores")
        }

        task.targetBackupSpec.forEach {
            TODO("Spec restores")
        }

        task.targetStorage.forEach { storageId ->
            val storage = storageManager.getStorage(storageId).blockingFirst()
            storage.content().take(1).flatMapIterable { it }.forEach { content ->
                val newest = content.versioning.getNewest()
                if (newest == null) {
                    Timber.tag(TAG).d("Empty BackupSpec: %s", content.backupSpec)
                } else {
                    val endpointFactory = restoreEndpointFactories[content.backupSpec.backupType]
                    val backupType = content.backupSpec.backupType
                    checkNotNull(endpointFactory) { "Unknown endpoint: type=$backupType (${content.backupSpec}" }
                    val endpoint = endpointFactory.create(progressChild)

                    val backupUnit = storage.load(content, newest.backupId)

                    val config: Restore.Config = task.restoreConfigs.find { it.restoreType == backupType }!!
                    endpoint.restore(config, backupUnit)
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

    private fun restoreBackup() {

    }

    @AssistedInject.Factory
    interface Factory : Processor.Factory<SimpleRestoreProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Restore", "Simple")
    }
}