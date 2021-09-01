package eu.darken.bb.onboarding

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC


class OnboardingActivityVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : VDC() {

    @AssistedFactory
    interface Factory : SavedStateVDCFactory<OnboardingActivityVDC>
}