package eu.darken.bb.storage.ui.list.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class StorageActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(StorageActionDialogVDC::class)
    abstract fun storageActionVDC(model: StorageActionDialogVDC.Factory): VDCFactory<out VDC>
}

