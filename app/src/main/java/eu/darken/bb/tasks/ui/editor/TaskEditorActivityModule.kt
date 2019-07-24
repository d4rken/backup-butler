package eu.darken.bb.tasks.ui.editor

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey

@Module
class TaskEditorActivityModule {

    @PerActivity
    @Provides
    @IntoMap
    @VDCKey(TaskEditorActivityVDC::class)
    fun taskActivity(factory: TaskEditorActivityVDC.Factory): VDCFactory<out VDC> {
        return factory
    }



}