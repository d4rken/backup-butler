package eu.darken.bb

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import eu.darken.bb.workers.WorkerBinder


@AppComponent.Scope
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    AndroidModule::class,
    ViewModelBinder::class,
    ServiceBinder::class,
    ReceiverBinder::class,
    ActivityBinder::class,
    AssistedInjectModule::class,
    WorkerBinder::class
])
interface AppComponent : AndroidInjector<App> {

    override fun inject(app: App)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    @MustBeDocumented
    @javax.inject.Scope
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Scope
}
