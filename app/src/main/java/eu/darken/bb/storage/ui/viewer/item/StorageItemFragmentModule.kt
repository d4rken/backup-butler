package eu.darken.bb.storage.ui.viewer.item

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class StorageItemFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StorageItemFragmentVDC::class)
    abstract fun storageContentVDC(model: StorageItemFragmentVDC.Factory): VDCFactory<out VDC>
}

