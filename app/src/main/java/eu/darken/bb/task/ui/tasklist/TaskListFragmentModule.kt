package eu.darken.bb.task.ui.tasklist

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.task.ui.tasklist.actions.TaskActionDialog
import eu.darken.bb.task.ui.tasklist.actions.TaskActionDialogModule


@Module
abstract class TaskListFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(TaskListFragmentVDC::class)
    abstract fun tasklistVDC(model: TaskListFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [TaskActionDialogModule::class])
    abstract fun taskEditDialog(): TaskActionDialog
}

