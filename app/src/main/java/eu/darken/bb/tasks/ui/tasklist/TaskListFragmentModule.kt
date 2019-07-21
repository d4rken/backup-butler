package eu.darken.bb.tasks.ui.tasklist

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class TaskListFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(TaskListFragmentVDC::class)
    abstract fun tasklistVDC(model: TaskListFragmentVDC.Factory): SavedStateVDCFactory<out VDC>
}

