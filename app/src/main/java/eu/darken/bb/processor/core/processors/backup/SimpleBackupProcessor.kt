package eu.darken.bb.processor.core.processors.backup

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.OpStatus
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.progress.updateProgressTertiary
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageFactory
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.Task
import timber.log.Timber

class SimpleBackupProcessor @AssistedInject constructor(
        @Assisted progressParent: Progress.Client,
        @AppContext context: Context,
        private val backupEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Backup.Endpoint.Factory<out Backup.Endpoint>>,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards Storage.Factory>,
        private val generators: @JvmSuppressWildcards Map<Backup.Type, Generator>,
        private val tmpDataRepo: TmpDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageRefRepo: StorageRefRepo
) : SimpleBaseProcessor(context, progressParent) {

    override fun doProcess(task: Task) {
        task as Task.Backup
        var success = 0
        var skipped = 0
        var error = 0
        task.sources.forEach { generatorId ->
            val generatorConfig = generatorRepo.get(generatorId)
                    .blockingGet()
                    .notNullValue(errorMessage = "Can't find generator config for $generatorId")
            // TODO what if the config has been deleted?

            progressParent.updateProgressSecondary(generatorConfig.label)

            val backupConfigs = generators.getValue(generatorConfig.generatorType).generate(generatorConfig)

            backupConfigs.forEach { config ->
                progressParent.updateProgressTertiary(config.getLabel(context))
                progressParent.updateProgressCount(Progress.Count.Counter(backupConfigs.indexOf(config) + 1, backupConfigs.size))

                val endpoint = backupEndpointFactories.getValue(config.backupType).create(progressChild)
                Timber.tag(TAG).i("Backing up %s using %s", config, endpoint)

                val backup = endpoint.backup(config)
                Timber.tag(TAG).i("Backup created: %s", backup)

                task.destinations.forEach { storageId ->
                    val storageRef = storageRefRepo.get(storageId)
                            .blockingGet()
                            .notNullValue(errorMessage = "Can't find storage for $storageId")
                    // TODO what if the storage has been deleted?

                    val repo = storageFactories.find { it.isCompatible(storageRef) }!!.create(storageRef, progressChild)
                    Timber.tag(TAG).i("Storing %s using %s", backup.id, repo)

                    val result = repo.save(backup)
                    success++
                    Timber.tag(TAG).i("Backup (%s) stored: %s", backup.id, result)
                }
                tmpDataRepo.deleteAll(backup.id)
            }
            progressParent.updateProgressTertiary("")
        }
        resultBuilder.primary(
                OpStatus(context).apply {
                    this.success = success
                    this.skipped = skipped
                    this.failed = error
                }.toDisplayString()
        )
    }

    override fun onCleanup() {
        tmpDataRepo.wipe()
        super.onCleanup()
    }

    @AssistedInject.Factory
    interface Factory : Processor.Factory<SimpleBackupProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Backup", "Simple")
    }
}