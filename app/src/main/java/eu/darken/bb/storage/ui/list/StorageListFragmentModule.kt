package eu.darken.bb.storage.ui.list

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.storage.ui.list.actions.StorageActionDialog
import eu.darken.bb.storage.ui.list.actions.StorageActionDialogModule


@Module
abstract class StorageListFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StorageListFragmentVDC::class)
    abstract fun repolistVDC(model: StorageListFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [StorageActionDialogModule::class])
    abstract fun storageEditDialog(): StorageActionDialog
}

