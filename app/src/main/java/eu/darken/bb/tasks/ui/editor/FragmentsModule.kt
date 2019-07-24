package eu.darken.bb.tasks.ui.editor

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.tasks.ui.editor.destinations.DestinationsFragment
import eu.darken.bb.tasks.ui.editor.destinations.DestinationsFragmentModule
import eu.darken.bb.tasks.ui.editor.intro.IntroFragment
import eu.darken.bb.tasks.ui.editor.intro.IntroFragmentModule
import eu.darken.bb.tasks.ui.editor.sources.SourcesFragment
import eu.darken.bb.tasks.ui.editor.sources.SourcesFragmentModule

@Module
abstract class FragmentsModule {
    @PerFragment
    @ContributesAndroidInjector(modules = [IntroFragmentModule::class])
    abstract fun introFragment(): IntroFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SourcesFragmentModule::class])
    abstract fun sourcesFragment(): SourcesFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
    abstract fun destinationsFragment(): DestinationsFragment
}