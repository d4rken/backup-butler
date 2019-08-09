package eu.darken.bb.storage.ui.viewer.details.page

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class DetailPageFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(DetailPageFragmentVDC::class)
    abstract fun detailsPageVDC(model: DetailPageFragmentVDC.Factory): VDCFactory<out VDC>
}

