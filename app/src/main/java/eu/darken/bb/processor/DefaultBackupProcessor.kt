package eu.darken.bb.processor

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Endpoint
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.processor.tmp.TmpDataRepo
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageFactory
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.DefaultTask
import eu.darken.bb.task.core.Task
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DefaultBackupProcessor @Inject constructor(
        private val endpointFactories: @JvmSuppressWildcards Map<Backup.Type, Endpoint.Factory>,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards Storage.Factory>,
        private val generators: @JvmSuppressWildcards Map<Backup.Type, Generator>,
        private val tmpDataRepo: TmpDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageRefRepo: StorageRefRepo
) {

    fun process(task: Task): Task.Result {
        Timber.tag(TAG).i("Processing backup task: %s", task)
        return try {
            doProcess(task)
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "Task failed: %s", task)
            DefaultTask.Result(task.taskId, exception)
        }
    }

    private fun doProcess(task: Task): Task.Result {
        task.sources.forEach { generatorId ->
            val generatorConfig = generatorRepo.get(generatorId)
                    .blockingGet()
                    .notNullValue(errorMessage = "Can't find generator config for $generatorId")
            // TODO what if the config has been deleted?

            val backupConfigs = generators.getValue(generatorConfig.generatorType).generate(generatorConfig)

            backupConfigs.forEach { config ->
                val endpoint = endpointFactories.getValue(config.backupType).create(config)
                Timber.tag(TAG).i("Backing up %s using %s", config, endpoint)

                val backup = endpoint.backup(config)
                Timber.tag(TAG).i("Backup created: %s", backup)

                task.destinations.forEach { storageId ->
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

        return DefaultTask.Result(task.taskId, Task.Result.State.SUCCESS)
    }

    companion object {
        private val TAG = App.logTag("Processor", "Default")
    }
}