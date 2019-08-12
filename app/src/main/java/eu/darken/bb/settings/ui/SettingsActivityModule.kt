package eu.darken.bb.settings.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.settings.ui.index.IndexPrefFragment
import eu.darken.bb.settings.ui.ui.UIPrefFragment
import eu.darken.bb.settings.ui.ui.UIPrefFragmentModule

@Module
abstract class SettingsActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(SettingsActivityVDC::class)
    abstract fun settingsActivity(factory: SettingsActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector
    abstract fun index(): IndexPrefFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [UIPrefFragmentModule::class])
    abstract fun ui(): UIPrefFragment
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
//    abstract fun destinationsFragment(): DestinationsFragment
}