package eu.darken.bb.main.ui.simple

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.VDC
import javax.inject.Inject

@HiltViewModel
class SimpleActivityVDC @Inject constructor(
    private val handle: SavedStateHandle
) : VDC() {

    data class State(val ready: Boolean)
}