package eu.darken.bb.main.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.SmartVDC
import javax.inject.Inject

@HiltViewModel
class SettingsActivityVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()