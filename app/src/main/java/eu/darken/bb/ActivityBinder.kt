package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivity
import eu.darken.bb.backup.ui.generator.editor.GeneratorEditorActivityModule
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.common.debug.recording.ui.RecorderActivity
import eu.darken.bb.common.debug.recording.ui.RecorderActivityModule
import eu.darken.bb.common.file.ui.picker.APathPickerActivity
import eu.darken.bb.common.file.ui.picker.APathPickerActivityModule
import eu.darken.bb.main.ui.advanced.AdvancedActivity
import eu.darken.bb.main.ui.advanced.AdvancedActivityModule
import eu.darken.bb.main.ui.settings.SettingsActivity
import eu.darken.bb.main.ui.settings.SettingsActivityModule
import eu.darken.bb.main.ui.simple.SimpleActivity
import eu.darken.bb.main.ui.simple.SimpleActivityModule
import eu.darken.bb.onboarding.OnboardingActivity
import eu.darken.bb.onboarding.OnboardingActivityModule
import eu.darken.bb.processor.ui.ProcessorActivity
import eu.darken.bb.processor.ui.ProcessorActivityModule
import eu.darken.bb.storage.ui.editor.StorageEditorActivity
import eu.darken.bb.storage.ui.editor.StorageEditorActivityModule
import eu.darken.bb.storage.ui.viewer.StorageViewerActivity
import eu.darken.bb.storage.ui.viewer.StorageViewerActivityModule
import eu.darken.bb.task.ui.editor.TaskEditorActivity
import eu.darken.bb.task.ui.editor.TaskEditorActivityModule


@Module
abstract class ActivityBinder {

    @PerActivity
    @ContributesAndroidInjector(modules = [SimpleActivityModule::class])
    abstract fun simpleActivity(): SimpleActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [AdvancedActivityModule::class])
    abstract fun advancedActivity(): AdvancedActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun settingsActivity(): SettingsActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [OnboardingActivityModule::class])
    abstract fun onboardingActivity(): OnboardingActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [TaskEditorActivityModule::class])
    abstract fun taskEditorActivity(): TaskEditorActivity

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
    @ContributesAndroidInjector(modules = [APathPickerActivityModule::class])
    abstract fun pathPickerActivity(): APathPickerActivity
}