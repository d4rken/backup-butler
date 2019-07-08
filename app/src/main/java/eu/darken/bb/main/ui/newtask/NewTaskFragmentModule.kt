package eu.darken.bb.main.ui.newtask

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class NewTaskFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(NewTaskFragmentViewModel::class)
    abstract fun exampleFragmentVDC(factory: NewTaskFragmentViewModel.Factory): SavedStateVDCFactory<out ViewModel>
}

