package eu.darken.bb.backup

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backup.backups.BackupEndpoint
import eu.darken.bb.backup.backups.EndpointFactory
import eu.darken.bb.backup.backups.app.AppBackupEndpoint
import eu.darken.bb.backup.backups.file.FileBackupEndpoint

@Module
abstract class BackupModule {
    @Binds
    @IntoSet
    @EndpointFactory
    abstract fun appEndpoint(endpoint: AppBackupEndpoint.Factory): BackupEndpoint.Factory

    @Binds
    @IntoSet
    @EndpointFactory
    abstract fun fileEndpoint(endpoint: FileBackupEndpoint.Factory): BackupEndpoint.Factory
}
