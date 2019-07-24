package eu.darken.bb.tasks.ui.tasklist.actions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class TaskActionDialogModule {
    @Binds
    @IntoMap
    @VDCKey(TaskActionDialogVDC::class)
    abstract fun taskactionVDC(model: TaskActionDialogVDC.Factory): VDCFactory<out VDC>
}

