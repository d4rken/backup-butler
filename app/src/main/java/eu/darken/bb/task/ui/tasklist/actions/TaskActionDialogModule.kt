package eu.darken.bb.task.ui.tasklist.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class TaskActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(TaskActionDialogVDC::class)
    abstract fun taskactionVDC(model: TaskActionDialogVDC.Factory): VDCFactory<out VDC>
}

