package eu.darken.bb.task.ui.editor.restore.config

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class RestoreConfigFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(RestoreConfigFragmentVDC::class)
    abstract fun restoreOptions(model: RestoreConfigFragmentVDC.Factory): VDCFactory<out VDC>
}

