package eu.darken.bb.backup.processor

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.EndpointFactory
import eu.darken.bb.backup.processor.cache.CacheRepo
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.RepoFactory
import eu.darken.bb.tasks.core.BackupTask
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DefaultBackupProcessor @Inject constructor(
        @EndpointFactory private val endpointFactories: Set<@JvmSuppressWildcards Backup.Endpoint.Factory>,
        @RepoFactory private val repoFactories: Set<@JvmSuppressWildcards BackupRepo.Factory>,
        private val cacheRepo: CacheRepo
) {
    companion object {
        private val TAG = App.logTag("DefaultBackupProcessor")
    }

    fun process(backupTask: BackupTask): BackupTask.Result {
        Timber.tag(TAG).i("Processing backup task: %s", backupTask)
        backupTask.sources.forEach { backupConf ->
            val endpoint = endpointFactories.find { it.isCompatible(backupConf) }!!.create(backupConf)
            Timber.tag(TAG).d("Backing up %s using %s", backupConf, endpoint)

            val backup = endpoint.backup(backupConf)
            Timber.tag(TAG).d("Backup created: %s", backup)

            backupTask.destinations.forEach { repoConf ->
                val repo = repoFactories.find { it.isCompatible(repoConf) }!!.create(repoConf)
                Timber.tag(TAG).d("Storing %s using %s", backup, repo)

                val result = repo.save(backup)
                Timber.tag(TAG).d("Backup piece %s stored: %s", backup, result)
            }

            cacheRepo.removeAll(backup.id)
        }

        Thread.sleep(5 * 1000)

        return DefaultBackupTask.Result("123", BackupTask.Result.State.SUCCESS)
    }
}