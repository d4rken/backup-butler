package eu.darken.bb.backup.ui.generator.list

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsActionDialog
import eu.darken.bb.backup.ui.generator.list.actions.GeneratorsActionDialogModule
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerChildFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey


@Module
abstract class GeneratorsFragmentModule {
    @Binds
    @IntoMap
    @VDCKey(GeneratorsFragmentVDC::class)
    abstract fun generatorsFragment(model: GeneratorsFragmentVDC.Factory): VDCFactory<out VDC>

    @PerChildFragment
    @ContributesAndroidInjector(modules = [GeneratorsActionDialogModule::class])
    abstract fun generatorsEditDialog(): GeneratorsActionDialog
}

