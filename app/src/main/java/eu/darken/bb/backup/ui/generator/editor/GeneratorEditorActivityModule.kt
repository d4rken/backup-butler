package eu.darken.bb.backup.ui.generator.editor

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backup.ui.generator.editor.types.GeneratorTypeFragment
import eu.darken.bb.backup.ui.generator.editor.types.GeneratorTypeFragmentModule
import eu.darken.bb.backup.ui.generator.editor.types.app.config.AppEditorConfigFragment
import eu.darken.bb.backup.ui.generator.editor.types.app.config.AppEditorConfigFragmentModule
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.AppEditorPreviewFragment
import eu.darken.bb.backup.ui.generator.editor.types.app.preview.AppEditorPreviewFragmentModule
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorFragment
import eu.darken.bb.backup.ui.generator.editor.types.files.FilesEditorFragmentModule
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey

@Module
abstract class GeneratorEditorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(GeneratorEditorActivityVDC::class)
    abstract fun generatorEditor(factory: GeneratorEditorActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [GeneratorTypeFragmentModule::class])
    abstract fun typeSelection(): GeneratorTypeFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AppEditorConfigFragmentModule::class])
    abstract fun appEditorConfig(): AppEditorConfigFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AppEditorPreviewFragmentModule::class])
    abstract fun appEditorPreview(): AppEditorPreviewFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [FilesEditorFragmentModule::class])
    abstract fun filesEditor(): FilesEditorFragment
}