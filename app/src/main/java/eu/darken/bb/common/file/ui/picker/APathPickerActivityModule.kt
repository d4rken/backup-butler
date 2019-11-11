package eu.darken.bb.common.file.ui.picker

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.file.ui.picker.local.LocalPickerFragment
import eu.darken.bb.common.file.ui.picker.local.LocalPickerFragmentModule
import eu.darken.bb.common.file.ui.picker.types.TypesPickerFragment
import eu.darken.bb.common.file.ui.picker.types.TypesPickerFragmentModule
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey

@Module
abstract class APathPickerActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(APathPickerActivityVDC::class)
    abstract fun pickerActivity(factory: APathPickerActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [TypesPickerFragmentModule::class])
    abstract fun types(): TypesPickerFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [LocalPickerFragmentModule::class])
    abstract fun local(): LocalPickerFragment
}