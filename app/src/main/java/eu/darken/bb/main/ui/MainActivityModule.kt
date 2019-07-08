package eu.darken.bb.main.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.main.ui.newtask.NewTaskFragment
import eu.darken.bb.main.ui.newtask.NewTaskFragmentModule
import eu.darken.bb.main.ui.overview.OverviewFragment
import eu.darken.bb.main.ui.overview.OverviewFragmentModule

@Module(includes = [OverviewFragmentModule::class])
abstract class MainActivityModule {

    @Binds
    @IntoMap
    @VDCKey(MainActivityVDC::class)
    abstract fun mainActivityVDC(model: MainActivityVDC.Factory): SavedStateVDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [OverviewFragmentModule::class])
    abstract fun exampleFragment(): OverviewFragment


    @ContributesAndroidInjector(modules = [NewTaskFragmentModule::class])
    abstract fun newTaskFragment(): NewTaskFragment
}