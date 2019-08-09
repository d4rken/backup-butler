package eu.darken.bb.storage.ui.viewer.content.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class ContentActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(ContentActionDialogVDC::class)
    abstract fun contentActionVDC(model: ContentActionDialogVDC.Factory): VDCFactory<out VDC>
}

