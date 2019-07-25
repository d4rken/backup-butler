package eu.darken.bb.backups

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backups.app.AppEndpoint
import eu.darken.bb.backups.file.FileEndpoint

@Module
abstract class BackupModule {
    @Binds
    @IntoSet
    @EndpointFactory
    abstract fun appEndpoint(endpoint: AppEndpoint.Factory): Endpoint.Factory

    @Binds
    @IntoSet
    @EndpointFactory
    abstract fun fileEndpoint(endpoint: FileEndpoint.Factory): Endpoint.Factory
}
