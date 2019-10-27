package eu.darken.bb.common.file.picker

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey

@Module
abstract class APathPickerActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(APathPickerActivityVDC::class)
    abstract fun pickerActivity(factory: APathPickerActivityVDC.Factory): VDCFactory<out VDC>
//
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
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [RestoreConfigFragmentModule::class])
//    abstract fun restoreConfig(): RestoreConfigFragment
//
//    @PerFragment
//    @ContributesAndroidInjector(modules = [RestoreSourcesFragmentModule::class])
//    abstract fun restoreSources(): RestoreSourcesFragment
}