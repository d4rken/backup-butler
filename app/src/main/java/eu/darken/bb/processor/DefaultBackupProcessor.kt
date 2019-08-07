package eu.darken.bb.processor

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backups.core.Backup
import eu.darken.bb.backups.core.Endpoint
import eu.darken.bb.backups.core.GeneratorRepo
import eu.darken.bb.backups.core.SpecGenerator
import eu.darken.bb.processor.tmp.TmpDataRepo
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageFactory
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.tasks.core.BackupTask
import eu.darken.bb.tasks.core.DefaultBackupTask
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DefaultBackupProcessor @Inject constructor(
        private val endpointFactories: @JvmSuppressWildcards Map<Backup.Type, Endpoint.Factory>,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards BackupStorage.Factory>,
        private val generators: @JvmSuppressWildcards Map<Backup.Type, SpecGenerator>,
        private val tmpDataRepo: TmpDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageRefRepo: StorageRefRepo
) {

    fun process(backupTask: BackupTask): BackupTask.Result {
        Timber.tag(TAG).i("Processing backup task: %s", backupTask)
        return try {
            doProcess(backupTask)
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "BackupTask failed: %s", backupTask)
            DefaultBackupTask.Result(backupTask.taskId, exception)
        }
    }

    private fun doProcess(backupTask: BackupTask): BackupTask.Result {
        backupTask.sources.forEach { generatorId ->
            val generatorConfig = generatorRepo.get(generatorId)
                    .blockingGet()
                    .notNullValue(errorMessage = "Can't find generator config for $generatorId")
            // TODO what if the config has been deleted?

            val backupConfigs = generators.getValue(generatorConfig.generatorType).generate(generatorConfig)

            backupConfigs.forEach { config ->
                val endpoint = endpointFactories.getValue(config.configType).create(config)
                Timber.tag(TAG).i("Backing up %s using %s", config, endpoint)

                val backup = endpoint.backup(config)
                Timber.tag(TAG).i("Backup created: %s", backup)

                backupTask.destinations.forEach { storageId ->
                    val storageRef = storageRefRepo.get(storageId)
                            .blockingGet()
                            .notNullValue(errorMessage = "Can't find storage for $storageId")
                    // TODO what if the storage has been deleted?

                    val repo = storageFactories.find { it.isCompatible(storageRef) }!!.create(storageRef)
                    Timber.tag(TAG).i("Storing %s using %s", backup.id, repo)

                    val result = repo.save(backup)
                    Timber.tag(TAG).i("Backup (%s) stored: %s", backup.id, result)
                }
                tmpDataRepo.deleteAll(backup.id)
            }
        }

        Thread.sleep(5 * 1000)

        return DefaultBackupTask.Result(backupTask.taskId, BackupTask.Result.State.SUCCESS)
    }

    companion object {
        private val TAG = App.logTag("Processor", "Default")
    }
}