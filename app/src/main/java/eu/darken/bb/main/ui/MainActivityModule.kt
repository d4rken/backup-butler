package eu.darken.bb.main.ui

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
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
    @VDCKey(MainActivityViewModel::class)
    abstract fun mainActivityVDC(model: MainActivityViewModel.Factory): SavedStateVDCFactory<out ViewModel>

    @ContributesAndroidInjector(modules = [OverviewFragmentModule::class])
    abstract fun exampleFragment(): OverviewFragment


    @ContributesAndroidInjector(modules = [NewTaskFragmentModule::class])
    abstract fun newTaskFragment(): NewTaskFragment
}