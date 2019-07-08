package eu.darken.bb.onboarding

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import eu.darken.bb.common.dagger.SavedStateVDCFactory
import eu.darken.bb.common.dagger.VDCKey
import eu.darken.bb.onboarding.steps.HelloStepFragment
import eu.darken.bb.onboarding.steps.HelloStepFragmentModule

@Module(includes = [HelloStepFragmentModule::class])
abstract class OnboardingActivityModule {

    @Binds
    @IntoMap
    @VDCKey(OnboardingActivityViewModel::class)
    abstract fun mainActivityVDC(model: OnboardingActivityViewModel.Factory): SavedStateVDCFactory<out ViewModel>

    @ContributesAndroidInjector(modules = [HelloStepFragmentModule::class])
    abstract fun helloStep(): HelloStepFragment

}