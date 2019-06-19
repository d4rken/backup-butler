package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.main.core.service.BackupService
import eu.darken.bb.main.core.service.BackupServiceModule


@Module
internal abstract class ServiceBinder {
    @ContributesAndroidInjector(modules = [BackupServiceModule::class])
    internal abstract fun backupService(): BackupService

}