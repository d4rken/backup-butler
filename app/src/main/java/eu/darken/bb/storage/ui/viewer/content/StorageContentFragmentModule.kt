package eu.darken.bb.storage.ui.viewer.content

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class StorageContentFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(StorageContentFragmentVDC::class)
    abstract fun storageContentVDC(model: StorageContentFragmentVDC.Factory): VDCFactory<out VDC>
}

