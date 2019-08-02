package eu.darken.bb.backups.ui.editor

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.backups.ui.editor.types.TypeSelectionFragment
import eu.darken.bb.backups.ui.editor.types.TypeSelectionFragmentModule
import eu.darken.bb.backups.ui.editor.types.app.AppEditorFragment
import eu.darken.bb.backups.ui.editor.types.app.AppEditorFragmentModule
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey

@Module
abstract class BackupEditorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(BackupEditorActivityVDC::class)
    abstract fun storageEditor(factory: BackupEditorActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [TypeSelectionFragmentModule::class])
    abstract fun typeSelection(): TypeSelectionFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [AppEditorFragmentModule::class])
    abstract fun appEditor(): AppEditorFragment

}