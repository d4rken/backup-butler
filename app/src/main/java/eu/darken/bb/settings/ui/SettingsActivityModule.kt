package eu.darken.bb.settings.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey

@Module
abstract class SettingsActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(SettingsActivityVDC::class)
    abstract fun settingsActivity(factory: SettingsActivityVDC.Factory): VDCFactory<out VDC>

//    @PerFragment
//    @ContributesAndroidInjector(modules = [IntroFragmentModule::class])
//    abstract fun introFragment(): IntroFragment
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [SourcesFragmentModule::class])
//    abstract fun sourcesFragment(): SourcesFragment
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
//    abstract fun destinationsFragment(): DestinationsFragment
}