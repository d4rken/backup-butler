package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppBackupEndpoint
import eu.darken.bb.backup.core.app.AppRestoreEndpoint
import eu.darken.bb.backup.core.file.FileBackupEndpoint
import eu.darken.bb.backup.core.file.FileRestoreEndpoint

@Module
abstract class BackupModule {
    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.APP)
    abstract fun appBackupEndpoint(endpoint: AppBackupEndpoint.Factory): Backup.Endpoint.Factory<out Backup.Endpoint>

    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.APP)
    abstract fun appRestoreEndpoint(endpoint: AppRestoreEndpoint.Factory): Restore.Endpoint.Factory<out Restore.Endpoint>

    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.FILE)
    abstract fun fileBackup(endpoint: FileBackupEndpoint.Factory): Backup.Endpoint.Factory<out Backup.Endpoint>

    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.FILE)
    abstract fun fileRestore(endpoint: FileRestoreEndpoint.Factory): Restore.Endpoint.Factory<out Restore.Endpoint>
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class EndpointFactory(val value: Backup.Type)