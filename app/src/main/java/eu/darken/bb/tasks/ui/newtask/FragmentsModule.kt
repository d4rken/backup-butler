package eu.darken.bb.tasks.ui.newtask

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.tasks.ui.newtask.destinations.DestinationsFragment
import eu.darken.bb.tasks.ui.newtask.destinations.DestinationsFragmentModule
import eu.darken.bb.tasks.ui.newtask.intro.IntroFragment
import eu.darken.bb.tasks.ui.newtask.intro.IntroFragmentModule
import eu.darken.bb.tasks.ui.newtask.sources.SourcesFragment
import eu.darken.bb.tasks.ui.newtask.sources.SourcesFragmentModule

@Module
abstract class FragmentsModule {
    @PerFragment
    @ContributesAndroidInjector(modules = [IntroFragmentModule::class])
    abstract fun newTaskFragment(): IntroFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SourcesFragmentModule::class])
    abstract fun sourcesFragment(): SourcesFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
    abstract fun destinationsFragment(): DestinationsFragment
}