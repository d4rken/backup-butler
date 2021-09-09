package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.backup.AppBackupEndpoint
import eu.darken.bb.backup.core.app.backup.BackupHandlerModule
import eu.darken.bb.backup.core.app.restore.AppRestoreEndpoint
import eu.darken.bb.backup.core.app.restore.RestoreHandlerModule
import eu.darken.bb.backup.core.files.FilesBackupEndpoint
import eu.darken.bb.backup.core.files.FilesRestoreEndpoint

@Module(includes = [RestoreHandlerModule::class, BackupHandlerModule::class])
@InstallIn(ServiceComponent::class)
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