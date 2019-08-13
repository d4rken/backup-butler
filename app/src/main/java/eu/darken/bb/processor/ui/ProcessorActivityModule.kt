package eu.darken.bb.processor.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.processor.ui.progress.ProgressFragment
import eu.darken.bb.processor.ui.progress.ProgressFragmentModule

@Module
abstract class ProcessorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(ProcessorActivityVDC::class)
    abstract fun processor(factory: ProcessorActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [ProgressFragmentModule::class])
    abstract fun progress(): ProgressFragment
}