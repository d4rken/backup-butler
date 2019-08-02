package eu.darken.bb.backups.core

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import eu.darken.bb.backups.core.app.AppEndpoint
import eu.darken.bb.backups.core.file.FileEndpoint

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
