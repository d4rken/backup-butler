package eu.darken.bb.storage.ui.viewer.content.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class ContentActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(ContentActionDialogVDC::class)
    abstract fun contentActionVDC(model: ContentActionDialogVDC.Factory): VDCFactory<out VDC>
}

