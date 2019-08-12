package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.debug.recording.core.RecorderService
import eu.darken.bb.processor.core.service.BackupService
import eu.darken.bb.processor.core.service.BackupServiceModule


@Module
internal abstract class ServiceBinder {
    @ContributesAndroidInjector(modules = [BackupServiceModule::class])
    internal abstract fun backupService(): BackupService

    @ContributesAndroidInjector
    internal abstract fun debugService(): RecorderService
}