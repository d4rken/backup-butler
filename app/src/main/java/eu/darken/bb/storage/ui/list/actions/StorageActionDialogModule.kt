package eu.darken.bb.storage.ui.list.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class StorageActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(StorageActionDialogVDC::class)
    abstract fun storageActionVDC(model: StorageActionDialogVDC.Factory): VDCFactory<out VDC>
}

