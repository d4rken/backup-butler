package eu.darken.bb.processor.core

import android.content.Context
import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.R
import eu.darken.bb.backup.core.Backup
import eu.darken.bb.backup.core.Endpoint
import eu.darken.bb.backup.core.Generator
import eu.darken.bb.backup.core.GeneratorRepo
import eu.darken.bb.common.HasContext
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.progress.Progress
import eu.darken.bb.common.progress.updateProgressCount
import eu.darken.bb.common.progress.updateProgressSecondary
import eu.darken.bb.common.progress.updateProgressTertiary
import eu.darken.bb.processor.core.tmp.TmpDataRepo
import eu.darken.bb.storage.core.Storage
import eu.darken.bb.storage.core.StorageFactory
import eu.darken.bb.storage.core.StorageRefRepo
import eu.darken.bb.task.core.DefaultTask
import eu.darken.bb.task.core.Task
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DefaultBackupProcessor @Inject constructor(
        @AppContext override val context: Context,
        private val endpointFactories: @JvmSuppressWildcards Map<Backup.Type, Endpoint.Factory<out Endpoint>>,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards Storage.Factory>,
        private val generators: @JvmSuppressWildcards Map<Backup.Type, Generator>,
        private val tmpDataRepo: TmpDataRepo,
        private val generatorRepo: GeneratorRepo,
        private val storageRefRepo: StorageRefRepo
) : HasContext {

    lateinit var progressParent: Progress.Client
    private val progressChild = object : Progress.Client {
        override fun updateProgress(update: (Progress.Data) -> Progress.Data) {
            progressParent.updateProgress { parent ->
                val oldChild = parent.child ?: Progress.Data()
                val newChild = update.invoke(oldChild)
                parent.copy(child = newChild)
            }
        }
    }

    fun process(task: Task): Task.Result {
        Timber.tag(TAG).i("Processing backup task: %s", task)
        return try {
            doProcess(task)
        } catch (exception: Exception) {
            Timber.tag(TAG).e(exception, "Task failed: %s", task)
            DefaultTask.Result(task.taskId, exception)
        } finally {
            progressParent.updateProgressSecondary(context, R.string.progress_working_label)
            progressParent.updateProgressCount(Progress.Count.Indeterminate())
            progressParent.updateProgress { it.copy(child = null) }
        }
    }

    private fun doProcess(task: Task): Task.Result {
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

                val endpoint = endpointFactories.getValue(config.backupType).create(progressChild)
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
                    Timber.tag(TAG).i("Backup (%s) stored: %s", backup.id, result)
                }
                tmpDataRepo.deleteAll(backup.id)
            }
            progressParent.updateProgressTertiary("")
        }


        return DefaultTask.Result(task.taskId, Task.Result.State.SUCCESS)
    }

    companion object {
        private val TAG = App.logTag("Processor", "Default")
    }
}