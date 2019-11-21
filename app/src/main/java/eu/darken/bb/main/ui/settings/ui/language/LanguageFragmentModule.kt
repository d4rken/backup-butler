package eu.darken.bb.main.ui.settings.ui.language

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey


@Module
abstract class LanguageFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(LanguageFragmentVDC::class)
    abstract fun ui(model: LanguageFragmentVDC.Factory): VDCFactory<out VDC>
}

