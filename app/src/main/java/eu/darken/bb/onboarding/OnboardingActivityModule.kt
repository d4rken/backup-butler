package eu.darken.bb.onboarding

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.common.vdc.VDCFactory
import eu.darken.bb.common.vdc.VDCKey
import eu.darken.bb.onboarding.steps.HelloStepFragment
import eu.darken.bb.onboarding.steps.HelloStepFragmentModule

@Module(includes = [HelloStepFragmentModule::class])
abstract class OnboardingActivityModule {

    @Binds
    @IntoMap
    @VDCKey(OnboardingActivityVDC::class)
    abstract fun mainActivityVDC(model: OnboardingActivityVDC.Factory): VDCFactory<out VDC>

    @ContributesAndroidInjector(modules = [HelloStepFragmentModule::class])
    abstract fun helloStep(): HelloStepFragment

}