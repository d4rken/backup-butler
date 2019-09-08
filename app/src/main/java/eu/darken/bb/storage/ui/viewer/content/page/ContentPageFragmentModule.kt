package eu.darken.bb.storage.ui.viewer.content.page

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class ContentPageFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(ContentPageFragmentVDC::class)
    abstract fun detailsPageVDC(model: ContentPageFragmentVDC.Factory): VDCFactory<out VDC>
}

