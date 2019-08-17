package eu.darken.bb.debug.recording.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey

@Module
abstract class RecorderActivityModule {

    @Binds
    @IntoMap
    @VDCKey(RecorderActivityVDC::class)
    abstract fun recorderActivityVDC(model: RecorderActivityVDC.Factory): VDCFactory<out VDC>
}