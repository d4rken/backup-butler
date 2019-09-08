package eu.darken.bb.storage.ui.viewer.item

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.storage.ui.viewer.item.actions.ItemActionDialog
import eu.darken.bb.storage.ui.viewer.item.actions.ItemActionDialogModule


@Module
abstract class StorageItemFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StorageItemFragmentVDC::class)
    abstract fun storageContentVDC(model: StorageItemFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [ItemActionDialogModule::class])
    abstract fun contentActionDialog(): ItemActionDialog
}

