package eu.darken.bb.onboarding

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC


class OnboardingActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : VDC() {

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<OnboardingActivityVDC>
}