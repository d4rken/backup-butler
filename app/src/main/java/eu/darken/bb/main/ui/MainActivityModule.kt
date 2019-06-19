package eu.darken.bb.main.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.main.ui.overview.OverviewFragmentModule

@Module(includes = [OverviewFragmentModule::class])
abstract class MainActivityModule {

    @ContributesAndroidInjector(modules = [OverviewFragmentModule::class])
    abstract fun exampleFragment(): OverviewFragment
}