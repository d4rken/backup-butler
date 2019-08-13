package eu.darken.bb.main.ui.settings

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.main.ui.settings.debug.DebugSettingsFragment
import eu.darken.bb.main.ui.settings.debug.DebugSettingsFragmentModule
import eu.darken.bb.main.ui.settings.userinterface.UISettingsFragment
import eu.darken.bb.main.ui.settings.userinterface.UISettingsFragmentModule
import eu.darken.bb.storage.ui.settings.StorageSettingsFragment
import eu.darken.bb.storage.ui.settings.StorageSettingsFragmentModule

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
    @ContributesAndroidInjector(modules = [UISettingsFragmentModule::class])
    abstract fun ui(): UISettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [StorageSettingsFragmentModule::class])
    abstract fun storage(): StorageSettingsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [DebugSettingsFragmentModule::class])
    abstract fun debug(): DebugSettingsFragment
}