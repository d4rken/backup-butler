package eu.darken.bb.storage.ui.viewer

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.storage.ui.viewer.content.ItemContentsFragment
import eu.darken.bb.storage.ui.viewer.content.ItemContentsFragmentModule
import eu.darken.bb.storage.ui.viewer.item.StorageItemFragment
import eu.darken.bb.storage.ui.viewer.item.StorageItemFragmentModule

@Module
abstract class StorageViewerActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(StorageViewerActivityVDC::class)
    abstract fun storageViewer(factory: StorageViewerActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [StorageItemFragmentModule::class])
    abstract fun storageContent(): StorageItemFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ItemContentsFragmentModule::class])
    abstract fun contentDetails(): ItemContentsFragment
}