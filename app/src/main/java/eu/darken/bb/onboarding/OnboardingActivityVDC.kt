package eu.darken.bb.onboarding

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.VDC
import javax.inject.Inject

@HiltViewModel
class OnboardingActivityVDC @Inject constructor(
    private val handle: SavedStateHandle
) : VDC()