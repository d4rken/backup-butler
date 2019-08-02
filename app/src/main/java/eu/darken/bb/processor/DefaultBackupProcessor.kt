package eu.darken.bb.processor

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backups.core.Endpoint
import eu.darken.bb.backups.core.EndpointFactory
import eu.darken.bb.processor.tmp.TmpDataRepo
import eu.darken.bb.storage.core.BackupStorage
import eu.darken.bb.storage.core.StorageFactory
import eu.darken.bb.tasks.core.BackupTask
import eu.darken.bb.tasks.core.DefaultBackupTask
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DefaultBackupProcessor @Inject constructor(
        @EndpointFactory private val endpointFactories: Set<@JvmSuppressWildcards Endpoint.Factory>,
        @StorageFactory private val storageFactories: Set<@JvmSuppressWildcards BackupStorage.Factory>,
        private val tmpDataRepo: TmpDataRepo
) {

    fun process(backupTask: BackupTask): BackupTask.Result {
        Timber.tag(TAG).i("Processing backup task: %s", backupTask)
        backupTask.sources.forEach { backupConf ->
            val endpoint = endpointFactories.find { it.isCompatible(backupConf) }!!.create(backupConf)
            Timber.tag(TAG).i("Backing up %s using %s", backupConf, endpoint)

            val backup = endpoint.backup(backupConf)
            Timber.tag(TAG).i("Backup created: %s", backup)

            backupTask.destinations.forEach { repoConf ->
                val repo = storageFactories.find { it.isCompatible(repoConf) }!!.create(repoConf)
                Timber.tag(TAG).i("Storing %s using %s", backup.id, repo)

                val result = repo.save(backup)
                Timber.tag(TAG).i("Backup (%s) stored: %s", backup.id, result)
//                val loadedBackup = repo.load(result, result.revisionConfig.revisions.first().backupId)
//                Timber.tag(TAG).i("Backup loaded: %s", loadedBackup)
            }

            tmpDataRepo.deleteAll(backup.id)
        }

        Thread.sleep(5 * 1000)

        return DefaultBackupTask.Result("123", BackupTask.Result.State.SUCCESS)
    }

    companion object {
        private val TAG = App.logTag("BackupProcessor", "Default")
    }
}