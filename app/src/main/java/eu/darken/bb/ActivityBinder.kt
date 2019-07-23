package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.common.dagger.PerActivity
import eu.darken.bb.main.ui.MainActivity
import eu.darken.bb.main.ui.MainActivityModule
import eu.darken.bb.onboarding.OnboardingActivity
import eu.darken.bb.onboarding.OnboardingActivityModule
import eu.darken.bb.tasks.ui.newtask.FragmentsModule
import eu.darken.bb.tasks.ui.newtask.NewTaskActivity
import eu.darken.bb.tasks.ui.newtask.NewTaskActivityModule


@Module
abstract class ActivityBinder {

    @PerActivity
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun mainActivity(): MainActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [OnboardingActivityModule::class])
    abstract fun onboardingActivity(): OnboardingActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [NewTaskActivityModule::class, FragmentsModule::class])
    abstract fun newtaskActivity(): NewTaskActivity
}