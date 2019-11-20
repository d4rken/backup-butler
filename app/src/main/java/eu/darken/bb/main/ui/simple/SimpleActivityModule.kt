package eu.darken.bb.main.ui.simple

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.main.ui.simple.start.StartFragment
import eu.darken.bb.main.ui.simple.start.StartFragmentModule

@Module
abstract class SimpleActivityModule {

    @Binds
    @IntoMap
    @VDCKey(SimpleActivityVDC::class)
    abstract fun simpleActivityVDC(model: SimpleActivityVDC.Factory): VDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [StartFragmentModule::class])
    abstract fun startFragment(): StartFragment

}