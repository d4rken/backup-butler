package eu.darken.bb.storage.ui.viewer.item.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class ItemActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(ItemActionDialogVDC::class)
    abstract fun contentActionVDC(model: ItemActionDialogVDC.Factory): VDCFactory<out VDC>
}

