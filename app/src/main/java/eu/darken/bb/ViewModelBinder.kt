package eu.darken.bb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.DaggerViewModelFactory
import eu.darken.bb.common.dagger.ViewModelKey
import eu.darken.bb.main.ui.MainActivityViewModel
import eu.darken.bb.main.ui.fragment.OverviewFragmentViewModel

@Module
internal abstract class ViewModelBinder {
    @Binds
    abstract fun bindFactory(viewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityVM(model: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OverviewFragmentViewModel::class)
    abstract fun bindExampleFragmentVM(model: OverviewFragmentViewModel): ViewModel

}