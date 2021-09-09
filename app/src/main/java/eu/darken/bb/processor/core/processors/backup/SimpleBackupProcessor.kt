package eu.darken.bb.processor.core.processors.backup

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.progress.*
import eu.darken.bb.common.rx.blockingGet2
import eu.darken.bb.common.rx.withScopeThis
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import eu.darken.bb.task.core.backup.SimpleBackupTask
import eu.darken.bb.task.core.results.LogEvent
import eu.darken.bb.task.core.results.SimpleResult
import timber.log.Timber
import javax.inject.Provider

class SimpleBackupProcessor @AssistedInject constructor(
    @Assisted progressParent: Progress.Client,
    @ApplicationContext context: Context,
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
        updateProgressCount(Progress.Count.Percent(currentBackupCount, totalBackupCount))

        val endpointCache = mutableMapOf<Backup.Type, Backup.Endpoint>()

        task.sources.forEach { generatorId ->
            val generatorConfig = generatorRepo.get(generatorId).blockingGet2()
            requireNotNull(generatorConfig) { "Can't find generator config for $generatorId" }
            // TODO what if the config has been deleted?

            val backupConfigs = generators.getValue(generatorConfig.generatorType).generate(generatorConfig)
            backupConfigs.forEach { config ->
                updateProgressTertiary { config.getLabel(it) }

                val endpoint = endpointCache.getOrPut(config.backupType) {
                    backupEndpointFactories.getValue(config.backupType).get().keepAliveWIth(this)
                }
                Timber.tag(TAG).i("Backing up %s using %s", config, endpoint)

                val logEvents = mutableListOf<LogEvent>()
                val backupUnit = endpoint.forwardProgressTo(progressChild).withScopeThis {
                    endpoint.backup(config) {
                        logEvents.add(it)
                    }
                }

                Timber.tag(TAG).i("Backup created: %s", backupUnit)

                task.destinations.forEach { storageId ->
                    // TODO what if the storage has been deleted?
                    val storage = storageManager.getStorage(storageId).blockingFirst()
                    storage.keepAliveWith(this)
                    Timber.tag(TAG).i("Storing %s using %s", backupUnit.backupId, storage)

                    val subResultBuilder = SimpleResult.SubResult.Builder()
                    try {
                        subResultBuilder.label(backupUnit.spec.getLabel(context)) // If there are errors before getting a better label

                        val result = storage.forwardProgressTo(progressChild).withScopeThis {
                            storage.save(backupUnit)
                        }

                        Timber.tag(TAG).i("Backup (%s) stored: %s", backupUnit.backupId, result)
                        subResultBuilder.sucessful()
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Error while saving backup to storage: %s", backupUnit, storage)
                        subResultBuilder.error(context, e)
                    } finally {
                        subResultBuilder.addLogEvents(logEvents)
                        resultBuilder.addSubResult(subResultBuilder)
                    }

                    updateProgressCount(Progress.Count.Percent(++currentBackupCount, totalBackupCount))
                }
                mmDataRepo.release(backupUnit.backupId)
            }
        }
    }

    override fun onCleanup() {
        mmDataRepo.releaseAll()
        super.onCleanup()
    }

    @AssistedFactory
    interface Factory : Processor.Factory<SimpleBackupProcessor>

    companion object {
        private val TAG = App.logTag("Processor", "Backup", "Simple")
    }
}