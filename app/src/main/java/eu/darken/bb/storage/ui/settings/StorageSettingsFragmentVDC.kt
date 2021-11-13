package eu.darken.bb.storage.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class StorageSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()