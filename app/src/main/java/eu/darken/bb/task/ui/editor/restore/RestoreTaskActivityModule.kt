package eu.darken.bb.task.ui.editor.restore

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragment
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragmentModule

@Module
abstract class RestoreTaskActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(RestoreTaskActivityVDC::class)
    abstract fun taskActivity(factory: RestoreTaskActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [RestoreConfigFragmentModule::class])
    abstract fun optionsFragment(): RestoreConfigFragment
}