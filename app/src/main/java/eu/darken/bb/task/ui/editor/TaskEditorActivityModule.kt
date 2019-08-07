package eu.darken.bb.task.ui.editor

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.task.ui.editor.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.destinations.DestinationsFragmentModule
import eu.darken.bb.task.ui.editor.intro.IntroFragment
import eu.darken.bb.task.ui.editor.intro.IntroFragmentModule
import eu.darken.bb.task.ui.editor.sources.SourcesFragment
import eu.darken.bb.task.ui.editor.sources.SourcesFragmentModule

@Module
abstract class TaskEditorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(TaskEditorActivityVDC::class)
    abstract fun taskActivity(factory: TaskEditorActivityVDC.Factory): VDCFactory<out VDC>

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