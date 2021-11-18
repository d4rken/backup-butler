package eu.darken.bb.backup.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class BackupSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()