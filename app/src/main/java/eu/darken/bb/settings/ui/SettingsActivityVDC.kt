package eu.darken.bb.settings.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class SettingsActivityVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()