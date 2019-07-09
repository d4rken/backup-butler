package eu.darken.bb.tasks.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.tasks.ui.newtaskcreation.NewTaskFragment
import eu.darken.bb.tasks.ui.newtaskcreation.NewTaskFragmentModule

@Module(includes = [NewTaskFragmentModule::class])
abstract class TaskActivityModule {

    @Binds
    @IntoMap
    @VDCKey(TaskActivityVDC::class)
    abstract fun taskActivity(model: TaskActivityVDC.Factory): SavedStateVDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [NewTaskFragmentModule::class])
    abstract fun newTaskFragment(): NewTaskFragment

}