package eu.darken.bb.backup.core

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.core.app.AppEndpoint

@Module
abstract class BackupModule {
    @Binds
    @IntoMap
    @EndpointFactory(Backup.Type.APP)
    abstract fun appEndpoint(endpoint: AppEndpoint.Factory): Endpoint.Factory
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class EndpointFactory(val value: Backup.Type)