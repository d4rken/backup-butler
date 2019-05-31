package eu.darken.bb.onboarding

import dagger.Module
import dagger.android.ContributesAndroidInjector
import eu.darken.bb.onboarding.steps.HelloStepFragment
import eu.darken.bb.onboarding.steps.HelloStepFragmentModule

@Module(includes = [HelloStepFragmentModule::class])
abstract class OnboardingActivityModule {

    @ContributesAndroidInjector(modules = [HelloStepFragmentModule::class])
    abstract fun helloStep(): HelloStepFragment

}