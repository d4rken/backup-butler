package eu.darken.bb.main.ui.overview

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class OverviewFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(OverviewFragmentViewModel::class)
    abstract fun overviewVDC(model: OverviewFragmentViewModel.Factory): SavedStateVDCFactory<out ViewModel>
}

