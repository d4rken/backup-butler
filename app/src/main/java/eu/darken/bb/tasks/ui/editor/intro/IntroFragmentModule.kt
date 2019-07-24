package eu.darken.bb.tasks.ui.editor.intro

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class IntroFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(IntroFragmentVDC::class)
    abstract fun newTaskVDC(model: IntroFragmentVDC.Factory): VDCFactory<out VDC>
}


