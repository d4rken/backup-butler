package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.backups.ui.generator.editor.GeneratorEditorActivity
import eu.darken.bb.backups.ui.generator.editor.GeneratorEditorActivityModule
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.main.ui.MainActivity
import eu.darken.bb.main.ui.MainActivityModule
import eu.darken.bb.onboarding.OnboardingActivity
import eu.darken.bb.onboarding.OnboardingActivityModule
import eu.darken.bb.storage.ui.editor.StorageEditorActivity
import eu.darken.bb.storage.ui.editor.StorageEditorActivityModule
import eu.darken.bb.tasks.ui.editor.TaskEditorActivity
import eu.darken.bb.tasks.ui.editor.TaskEditorActivityModule


@Module
abstract class ActivityBinder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun mainActivity(): MainActivity

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
}