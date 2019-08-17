package eu.darken.bb.task.ui.editor.intro

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class IntroFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(IntroFragmentVDC::class)
    abstract fun newTaskVDC(model: IntroFragmentVDC.Factory): VDCFactory<out VDC>
}


