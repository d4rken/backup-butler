package eu.darken.bb.processor.core.processors.backup

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.*
import eu.darken.bb.common.rx.blockingGet2
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.backup.SimpleBackupTask
import eu.darken.bb.task.core.results.IOEvent
import eu.darken.bb.task.core.results.SimpleResult
import timber.log.Timber
import javax.inject.Provider

class SimpleBackupProcessor @AssistedInject constructor(
        @Assisted progressParent: Progress.Client,
        @AppContext context: Context,
        private val backupEndpointFactories: @JvmSuppressWildcards Map<Backup.Type, Provider<Backup.Endpoint>>,
        private val generators: @JvmSuppressWildcards Map<Backup.Type, Generator>,
        private val mmDataRepo: MMDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageManager: StorageManager
) : SimpleBaseProcessor(context, progressParent) {

    override fun updateProgress(update: (Progress.Data) -> Progress.Data) = progressParent.updateProgress(update)

    override fun doProcess(task: Task) {
        updateProgressPrimary(task.taskType.labelRes)
        updateProgressSecondary(task.label)

        task as SimpleBackupTask
        val totalBackupCount = task.destinations.size * task.sources.size
        var currentBackupCount = 0
        updateProgressCount(Progress.Count.Counter(currentBackupCount, totalBackupCount))

        task.sources.forEach { generatorId ->
            val generatorConfig = generatorRepo.get(generatorId).blockingGet2()
            requireNotNull(generatorConfig) { "Can't find generator config for $generatorId" }
            // TODO what if the config has been deleted?

            val backupConfigs = generators.getValue(generatorConfig.generatorType).generate(generatorConfig)
            backupConfigs.forEach { config ->
                updateProgressTertiary { config.getLabel(it) }

                backupEndpointFactories.getValue(config.backupType).get().use { endpoint ->
                    Timber.tag(TAG).i("Backing up %s using %s", config, endpoint)

                    val endpointProgressSub = endpoint.forwardProgressTo(progressChild)

                    val logActions = mutableListOf<IOEvent>()

                    val backupUnit = endpoint.backup(config) {
                        logActions.add(it)
                    }

                    Timber.tag(TAG).i("Backup created: %s", backupUnit)

                    endpointProgressSub.dispose()

                    task.destinations.forEach { storageId ->
                        // TODO what if the storage has been deleted?
                        val storage = storageManager.getStorage(storageId).blockingFirst()
                        Timber.tag(TAG).i("Storing %s using %s", backupUnit.backupId, storage)

                        val subResultBuilder = SimpleResult.SimpleSubResult.Builder()
                        try {
                            subResultBuilder.label(backupUnit.spec.getLabel(context)) // If there are errors before getting a better label

                            val storageProgressSub = storage.forwardProgressTo(progressChild)
                            val result = storage.save(backupUnit)
                            storageProgressSub.dispose()

                            Timber.tag(TAG).i("Backup (%s) stored: %s", backupUnit.backupId, result)
                            subResultBuilder.sucessful()
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Error while saving backup to storage: %s", backupUnit, storage)
                            subResultBuilder.error(context, e)
                        } finally {
                            resultBuilder.addSubResult(subResultBuilder)
                        }

                        updateProgressCount(Progress.Count.Counter(++currentBackupCount, totalBackupCount))
                    }
                    mmDataRepo.release(backupUnit.backupId)
                }

            }
        }
    }

    override fun onCleanup() {
        mmDataRepo.releaseAll()
        super.onCleanup()
    }

    @AssistedInject.Factory
    interface Factory : Processor.Factory<SimpleBackupProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Backup", "Simple")
    }
}