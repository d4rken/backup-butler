package eu.darken.bb.task.ui.editor.common.requirements

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class RequirementsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(RequirementsFragmentVDC::class)
    abstract fun newTaskVDC(model: RequirementsFragmentVDC.Factory): VDCFactory<out VDC>
}


