package eu.darken.bb.backup

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.backups.Backup
import eu.darken.bb.backup.backups.EndpointFactory
import eu.darken.bb.backup.backups.app.AppBackupEndpoint
import eu.darken.bb.backup.backups.file.FileBackupEndpoint
import eu.darken.bb.backup.repos.BackupRepo
import eu.darken.bb.backup.repos.RepoFactory
import eu.darken.bb.backup.repos.local.LocalStorageRepo

@Module
abstract class BackupModule {
    @Binds
    @IntoSet
    @EndpointFactory
    abstract fun appEndpoint(endpoint: AppBackupEndpoint.Factory): Backup.Endpoint.Factory

    @Binds
    @IntoSet
    @EndpointFactory
    abstract fun fileEndpoint(endpoint: FileBackupEndpoint.Factory): Backup.Endpoint.Factory

    @Binds
    @IntoSet
    @RepoFactory
    abstract fun localRepo(repo: LocalStorageRepo.Factory): BackupRepo.Factory
}
