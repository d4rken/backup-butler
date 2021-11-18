package eu.darken.bb.processor.core.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import eu.darken.bb.backup.core.BackupModule
import eu.darken.bb.processor.core.ProcessorCoroutineScope
import eu.darken.bb.processor.core.ProcessorScope
import kotlinx.coroutines.CoroutineScope

@InstallIn(ServiceComponent::class)
@Module(includes = [BackupModule::class])
abstract class ProcessorServiceModule {

    @Binds
    @ProcessorScope
    abstract fun processorScope(scope: ProcessorCoroutineScope): CoroutineScope

}