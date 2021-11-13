package eu.darken.bb.settings.ui.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class UISettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()