package eu.darken.bb.storage.ui.editor

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.VDC
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.dagger.VDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.storage.ui.editor.types.TypeSelectionFragment
import eu.darken.bb.storage.ui.editor.types.TypeSelectionFragmentModule
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragment
import eu.darken.bb.storage.ui.editor.types.local.LocalEditorFragmentModule

@Module
abstract class StorageEditorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(StorageEditorActivityVDC::class)
    abstract fun storageEditor(factory: StorageEditorActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [TypeSelectionFragmentModule::class])
    abstract fun typeSelection(): TypeSelectionFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [LocalEditorFragmentModule::class])
    abstract fun localStorageEditor(): LocalEditorFragment

}