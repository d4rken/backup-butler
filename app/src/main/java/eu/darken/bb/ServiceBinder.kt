package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.common.debug.recording.core.RecorderService
import eu.darken.bb.processor.core.service.ProcessorService
import eu.darken.bb.processor.core.service.ProcessorServiceModule


@Module
internal abstract class ServiceBinder {
    @ContributesAndroidInjector(modules = [ProcessorServiceModule::class])
    internal abstract fun backupService(): ProcessorService

    @ContributesAndroidInjector
    internal abstract fun debugService(): RecorderService
}