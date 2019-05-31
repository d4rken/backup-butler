package eu.darken.bb

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.main.ui.MainActivity
import eu.darken.bb.main.ui.MainActivityModule
import eu.darken.bb.onboarding.OnboardingActivity


@Module
abstract class ActivityBinder {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun binOnboardingActivity(): OnboardingActivity

}