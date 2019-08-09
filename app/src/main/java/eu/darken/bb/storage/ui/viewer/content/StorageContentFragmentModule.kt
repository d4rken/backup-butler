package eu.darken.bb.storage.ui.viewer.content

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.storage.ui.viewer.content.actions.ContentActionDialog
import eu.darken.bb.storage.ui.viewer.content.actions.ContentActionDialogModule


@Module
abstract class StorageContentFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StorageContentFragmentVDC::class)
    abstract fun storageContentVDC(model: StorageContentFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [ContentActionDialogModule::class])
    abstract fun contentActionDialog(): ContentActionDialog
}

