package eu.darken.bb.trigger.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class TriggerSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()