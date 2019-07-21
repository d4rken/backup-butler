package eu.darken.bb.backup.processor

import dagger.Reusable
import eu.darken.bb.App
import eu.darken.bb.backup.backups.BackupEndpoint
import eu.darken.bb.backup.backups.EndpointFactory
import eu.darken.bb.backup.processor.tmp.TmpDataRepo
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.RepoFactory
import eu.darken.bb.tasks.core.BackupTask
import eu.darken.bb.tasks.core.DefaultBackupTask
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DefaultBackupProcessor @Inject constructor(
        @EndpointFactory private val endpointFactories: Set<@JvmSuppressWildcards BackupEndpoint.Factory>,
        @RepoFactory private val repoFactories: Set<@JvmSuppressWildcards BackupRepo.Factory>,
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
                val repo = repoFactories.find { it.isCompatible(repoConf) }!!.create(repoConf)
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