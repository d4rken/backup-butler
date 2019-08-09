package eu.darken.bb.storage.ui.viewer

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.storage.ui.viewer.content.StorageContentFragment
import eu.darken.bb.storage.ui.viewer.content.StorageContentFragmentModule
import eu.darken.bb.storage.ui.viewer.details.ContentDetailsFragment
import eu.darken.bb.storage.ui.viewer.details.ContentDetailsFragmentModule

@Module
abstract class StorageViewerActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(StorageViewerActivityVDC::class)
    abstract fun storageViewer(factory: StorageViewerActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [StorageContentFragmentModule::class])
    abstract fun storageContent(): StorageContentFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ContentDetailsFragmentModule::class])
    abstract fun contentDetails(): ContentDetailsFragment
}