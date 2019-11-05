package eu.darken.bb.task.ui.settings

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class TaskSettingsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(TaskSettingsFragmentVDC::class)
    abstract fun ui(model: TaskSettingsFragmentVDC.Factory): VDCFactory<out VDC>
}

