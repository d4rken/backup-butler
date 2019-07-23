package eu.darken.bb

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import eu.darken.bb.common.dagger.AssistedInjectModule
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.repos.core.RepoModule
import eu.darken.bb.workers.WorkerBinder


@PerApp
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AssistedInjectModule::class,
    AndroidModule::class,
    ServiceBinder::class,
    ReceiverBinder::class,
    ActivityBinder::class,
    AppModule::class,
    WorkerBinder::class,
    RepoModule::class
])
interface AppComponent : AndroidInjector<App> {

    @Component.Factory
    interface Factory : AndroidInjector.Factory<App>

}
