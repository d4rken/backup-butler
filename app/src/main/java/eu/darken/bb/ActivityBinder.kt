package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.main.ui.MainActivity
import eu.darken.bb.main.ui.MainActivityModule
import eu.darken.bb.onboarding.OnboardingActivity
import eu.darken.bb.onboarding.OnboardingActivityModule


@Module
abstract class ActivityBinder {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [OnboardingActivityModule::class])
    abstract fun onboardingActivity(): OnboardingActivity

}