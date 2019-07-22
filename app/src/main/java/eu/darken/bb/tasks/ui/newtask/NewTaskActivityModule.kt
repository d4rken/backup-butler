package eu.darken.bb.tasks.ui.newtask

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.tasks.ui.newtask.destinations.DestinationsFragment
import eu.darken.bb.tasks.ui.newtask.destinations.DestinationsFragmentModule
import eu.darken.bb.tasks.ui.newtask.intro.IntroFragment
import eu.darken.bb.tasks.ui.newtask.intro.IntroFragmentModule
import eu.darken.bb.tasks.ui.newtask.sources.SourcesFragment
import eu.darken.bb.tasks.ui.newtask.sources.SourcesFragmentModule

@Module(includes = [IntroFragmentModule::class])
abstract class NewTaskActivityModule {

    @Binds
    @IntoMap
    @VDCKey(NewTaskActivityVDC::class)
    abstract fun taskActivity(model: NewTaskActivityVDC.Factory): SavedStateVDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [IntroFragmentModule::class])
    abstract fun newTaskFragment(): IntroFragment

    @ContributesAndroidInjector(modules = [SourcesFragmentModule::class])
    abstract fun sourcesFragment(): SourcesFragment

    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
    abstract fun destinationsFragment(): DestinationsFragment
}