package eu.darken.bb

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import eu.darken.bb.backup.core.BackupTypeModule
import eu.darken.bb.common.dagger.AssistedInjectModule
import eu.darken.bb.common.dagger.PerApp
import eu.darken.bb.common.debug.DebugModuleModule
import eu.darken.bb.common.previews.GlideConfigModule
import eu.darken.bb.main.ui.MainActivity
import eu.darken.bb.processor.core.ProcessorModule
import eu.darken.bb.storage.core.StorageTypeModule
import eu.darken.bb.task.core.TaskTypeModule
import eu.darken.bb.workers.WorkerBinder


@PerApp
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AssistedInjectModule::class,
        AndroidModule::class,
        ServiceBinder::class,
        ReceiverBinder::class,
        ActivityBinder::class,
        AppModule::class,
        DebugModuleModule::class,
        WorkerBinder::class,
        StorageTypeModule::class,
        BackupTypeModule::class,
        TaskTypeModule::class,
        ProcessorModule::class
    ]
)
interface AppComponent : AndroidInjector<App> {

    fun inject(main: MainActivity)

    fun inject(glide: GlideConfigModule)

    @Component.Factory
    interface Factory : AndroidInjector.Factory<App>

}
