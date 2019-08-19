package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivity
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivityModule
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.debug.recording.ui.RecorderActivity
import eu.darken.bb.debug.recording.ui.RecorderActivityModule
import eu.darken.bb.main.ui.settings.SettingsActivity
import eu.darken.bb.main.ui.settings.SettingsActivityModule
import eu.darken.bb.main.ui.start.MainActivity
import eu.darken.bb.main.ui.start.MainActivityModule
import eu.darken.bb.onboarding.OnboardingActivity
import eu.darken.bb.onboarding.OnboardingActivityModule
import eu.darken.bb.processor.ui.ProcessorActivity
import eu.darken.bb.processor.ui.ProcessorActivityModule
import eu.darken.bb.storage.ui.editor.StorageEditorActivity
import eu.darken.bb.storage.ui.editor.StorageEditorActivityModule
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityModule
import eu.darken.bb.task.ui.editor.backup.BackupTaskActivity
import eu.darken.bb.task.ui.editor.backup.BackupTaskActivityModule
import eu.darken.bb.task.ui.editor.restore.RestoreTaskActivity
import eu.darken.bb.task.ui.editor.restore.RestoreTaskActivityModule


@Module
abstract class ActivityBinder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun mainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun settingsActivity(): SettingsActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [OnboardingActivityModule::class])
    abstract fun onboardingActivity(): OnboardingActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [BackupTaskActivityModule::class])
    abstract fun taskEditorActivity(): BackupTaskActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [StorageEditorActivityModule::class])
    abstract fun storageEditorActivity(): StorageEditorActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [GeneratorEditorActivityModule::class])
    abstract fun backupEditorActivity(): GeneratorEditorActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [StorageViewerActivityModule::class])
    abstract fun storageViewerActivity(): StorageViewerActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [RecorderActivityModule::class])
    abstract fun recoderActivity(): RecorderActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [ProcessorActivityModule::class])
    abstract fun processorActivity(): ProcessorActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [RestoreTaskActivityModule::class])
    abstract fun restoreActivity(): RestoreTaskActivity
}