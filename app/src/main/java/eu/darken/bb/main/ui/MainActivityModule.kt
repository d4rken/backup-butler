package eu.darken.bb.main.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.main.ui.fragment.ExampleFragment
import eu.darken.bb.main.ui.fragment.ExampleFragmentModule

@Module(includes = [ExampleFragmentModule::class])
abstract class MainActivityModule {

    @ContributesAndroidInjector(modules = [ExampleFragmentModule::class])
    abstract fun exampleFragment(): ExampleFragment
}