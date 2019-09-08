package eu.darken.bb.storage.ui.viewer.content

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.storage.ui.viewer.content.page.ContentPageFragment
import eu.darken.bb.storage.ui.viewer.content.page.ContentPageFragmentModule


@Module
abstract class ItemContentsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(ItemContentsFragmentVDC::class)
    abstract fun contentDetailsVDC(model: ItemContentsFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [ContentPageFragmentModule::class])
    abstract fun detailPage(): ContentPageFragment
}

