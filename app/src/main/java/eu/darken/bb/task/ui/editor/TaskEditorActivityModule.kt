package eu.darken.bb.task.ui.editor

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.dagger.PerFragment
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragment
import eu.darken.bb.task.ui.editor.backup.destinations.DestinationsFragmentModule
import eu.darken.bb.task.ui.editor.backup.destinations.picker.StoragePickerFragment
import eu.darken.bb.task.ui.editor.backup.destinations.picker.StoragePickerFragmentModule
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragment
import eu.darken.bb.task.ui.editor.backup.intro.IntroFragmentModule
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragment
import eu.darken.bb.task.ui.editor.backup.sources.SourcesFragmentModule
import eu.darken.bb.task.ui.editor.backup.sources.picker.GeneratorPickerFragment
import eu.darken.bb.task.ui.editor.backup.sources.picker.GeneratorPickerFragmentModule
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragment
import eu.darken.bb.task.ui.editor.restore.config.RestoreConfigFragmentModule
import eu.darken.bb.task.ui.editor.restore.sources.RestoreSourcesFragment
import eu.darken.bb.task.ui.editor.restore.sources.RestoreSourcesFragmentModule

@Module
abstract class TaskEditorActivityModule {

    @PerActivity
    @Binds
    @IntoMap
    @VDCKey(TaskEditorActivityVDC::class)
    abstract fun taskActivity(factory: TaskEditorActivityVDC.Factory): VDCFactory<out VDC>

    @PerFragment
    @ContributesAndroidInjector(modules = [IntroFragmentModule::class])
    abstract fun introFragment(): IntroFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [SourcesFragmentModule::class])
    abstract fun sourcesFragment(): SourcesFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [GeneratorPickerFragmentModule::class])
    abstract fun generatorPicker(): GeneratorPickerFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [DestinationsFragmentModule::class])
    abstract fun destinationsFragment(): DestinationsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [StoragePickerFragmentModule::class])
    abstract fun storagePicker(): StoragePickerFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [RestoreConfigFragmentModule::class])
    abstract fun restoreConfig(): RestoreConfigFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [RestoreSourcesFragmentModule::class])
    abstract fun restoreSources(): RestoreSourcesFragment
}