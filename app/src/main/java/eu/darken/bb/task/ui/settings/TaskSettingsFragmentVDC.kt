package eu.darken.bb.task.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.smart.SmartVDC
import javax.inject.Inject

@HiltViewModel
class TaskSettingsFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle
) : SmartVDC()