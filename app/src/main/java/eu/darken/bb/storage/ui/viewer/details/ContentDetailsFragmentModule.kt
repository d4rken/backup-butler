package eu.darken.bb.storage.ui.viewer.details

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.storage.ui.viewer.details.page.DetailPageFragment
import eu.darken.bb.storage.ui.viewer.details.page.DetailPageFragmentModule


@Module
abstract class ContentDetailsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(ContentDetailsFragmentVDC::class)
    abstract fun contentDetailsVDC(model: ContentDetailsFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [DetailPageFragmentModule::class])
    abstract fun detailPage(): DetailPageFragment
}

