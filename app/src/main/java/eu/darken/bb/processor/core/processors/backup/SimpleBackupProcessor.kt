package eu.darken.bb.processor.core.processors.backup

import android.content.Context
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.OpStatus
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.*
import eu.darken.bb.common.rx.blockingGet2
import eu.darken.bb.processor.core.Processor
import eu.darken.bb.processor.core.mm.MMDataRepo
import eu.darken.bb.processor.core.processors.SimpleBaseProcessor
import eu.darken.bb.storage.core.StorageManager
import eu.darken.bb.task.core.Task
import io.reactivex.schedulers.Schedulers
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

    override fun doProcess(task: Task) {
        progressParent.updateProgressPrimary(R.string.progress_processing_x_label, task.label)
        progressParent.updateProgressSecondary { task.getDescription(it) }

        task as Task.Backup

        var success = 0
        var skipped = 0
        var error = 0
        task.sources.forEach { generatorId ->
            val generatorConfig = generatorRepo.get(generatorId).blockingGet2()
            requireNotNull(generatorConfig) { "Can't find generator config for $generatorId" }
            // TODO what if the config has been deleted?

            progressParent.updateProgressSecondary(generatorConfig.label)

            val backupConfigs = generators.getValue(generatorConfig.generatorType).generate(generatorConfig)

            backupConfigs.forEach { config ->
                progressParent.updateProgressTertiary { config.getLabel(it) }
                progressParent.updateProgressCount(Progress.Count.Counter(backupConfigs.indexOf(config) + 1, backupConfigs.size))

                backupEndpointFactories.getValue(config.backupType).get().use { endpoint ->
                    Timber.tag(TAG).i("Backing up %s using %s", config, endpoint)

                    val endpointProgressSub = endpoint.progress
                            .subscribeOn(Schedulers.io())
                            .subscribe { pro -> progressChild.updateProgress { pro } }

                    val backup = endpoint.backup(config)
                    Timber.tag(TAG).i("Backup created: %s", backup)

                    endpointProgressSub.dispose()

                    task.destinations.forEach { storageId ->
                        // TODO what if the storage has been deleted?
                        val repo = storageManager.getStorage(storageId).blockingFirst()
                        Timber.tag(TAG).i("Storing %s using %s", backup.backupId, repo)

                        val storageProgressSub = repo.progress
                                .subscribeOn(Schedulers.io())
                                .subscribe { pro -> progressChild.updateProgress { pro } }

                        val result = repo.save(backup)
                        success++
                        Timber.tag(TAG).i("Backup (%s) stored: %s", backup.backupId, result)

                        storageProgressSub.dispose()
                    }
                    mmDataRepo.release(backup.backupId)
                }

            }
            progressParent.updateProgressTertiary("")
        }
        resultBuilder.primary(OpStatus(success, skipped, error).toDisplayString(context))
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