package eu.darken.bb.main.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.VDC
import eu.darken.bb.main.core.UISettings
import javax.inject.Inject

@HiltViewModel
class MainActivityVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val uiSettings: UISettings,
) : VDC()