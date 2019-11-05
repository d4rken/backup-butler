package eu.darken.bb.main.ui.settings

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.ui.settings.BackupSettingsFragment
import eu.darken.bb.backup.ui.settings.BackupSettingsFragmentModule
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.settings.acks.AcknowledgementsFragment
import eu.darken.bb.main.ui.settings.debug.AcknowledgementsFragmentModule
import eu.darken.bb.main.ui.settings.debug.GeneralSettingsFragmentModule
import eu.darken.bb.main.ui.settings.general.GeneralSettingsFragment
import eu.darken.bb.main.ui.settings.support.SupportFragment
import eu.darken.bb.main.ui.settings.support.SupportFragmentModule
import eu.darken.bb.main.ui.settings.ui.UISettingsFragment
import eu.darken.bb.main.ui.settings.ui.UISettingsFragmentModule
import eu.darken.bb.schedule.ui.settings.SchedulerSettingsFragment
import eu.darken.bb.schedule.ui.settings.SchedulerSettingsFragmentModule
import eu.darken.bb.storage.ui.settings.StorageSettingsFragment
import eu.darken.bb.storage.ui.settings.StorageSettingsFragmentModule
import eu.darken.bb.task.ui.settings.TaskSettingsFragment
import eu.darken.bb.task.ui.settings.TaskSettingsFragmentModule
import eu.darken.bb.upgrades.ui.settings.AccountSettingsFragment
import eu.darken.bb.upgrades.ui.settings.AccountSettingsFragmentModule

@Module
abstract class SettingsActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(SettingsActivityVDC::class)
    abstract fun settingsActivity(factory: SettingsActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector
    abstract fun index(): IndexFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [GeneralSettingsFragmentModule::class])
    abstract fun general(): GeneralSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [UISettingsFragmentModule::class])
    abstract fun ui(): UISettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AccountSettingsFragmentModule::class])
    abstract fun account(): AccountSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SupportFragmentModule::class])
    abstract fun support(): SupportFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [StorageSettingsFragmentModule::class])
    abstract fun storage(): StorageSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [TaskSettingsFragmentModule::class])
    abstract fun task(): TaskSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SchedulerSettingsFragmentModule::class])
    abstract fun scheduler(): SchedulerSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [BackupSettingsFragmentModule::class])
    abstract fun backup(): BackupSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AcknowledgementsFragmentModule::class])
    abstract fun acknowledgements(): AcknowledgementsFragment
}