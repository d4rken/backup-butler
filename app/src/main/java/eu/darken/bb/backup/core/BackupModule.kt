package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppBackupEndpoint
import eu.darken.bb.backup.core.app.AppRestoreEndpoint
import eu.darken.bb.backup.core.files.FilesBackupEndpoint
import eu.darken.bb.backup.core.files.FilesRestoreEndpoint

@Module
abstract class BackupModule {
    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.APP)
    abstract fun appBackupEndpoint(endpoint: AppBackupEndpoint): Backup.Endpoint

    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.APP)
    abstract fun appRestoreEndpoint(endpoint: AppRestoreEndpoint): Restore.Endpoint

    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.FILES)
    abstract fun fileBackup(endpoint: FilesBackupEndpoint): Backup.Endpoint

    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.FILES)
    abstract fun fileRestore(endpoint: FilesRestoreEndpoint): Restore.Endpoint
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class EndpointFactory(val value: Backup.Type)