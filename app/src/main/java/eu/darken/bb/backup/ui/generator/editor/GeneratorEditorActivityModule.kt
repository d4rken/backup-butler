package eu.darken.bb.backup.ui.generator.editor

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.ui.generator.editor.types.TypeSelectionFragment
import eu.darken.bb.backup.ui.generator.editor.types.TypeSelectionFragmentModule
import eu.darken.bb.backup.ui.generator.editor.types.app.AppEditorFragment
import eu.darken.bb.backup.ui.generator.editor.types.app.AppEditorFragmentModule
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorFragment
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorFragmentModule
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey

@Module
abstract class GeneratorEditorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(GeneratorEditorActivityVDC::class)
    abstract fun generatorEditor(factory: GeneratorEditorActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [TypeSelectionFragmentModule::class])
    abstract fun typeSelection(): TypeSelectionFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AppEditorFragmentModule::class])
    abstract fun appEditor(): AppEditorFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [FilesEditorFragmentModule::class])
    abstract fun filesEditor(): FilesEditorFragment
}