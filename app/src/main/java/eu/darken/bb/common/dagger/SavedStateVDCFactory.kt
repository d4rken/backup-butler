package eu.darken.bb.common.dagger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

interface SavedStateVDCFactory<T : ViewModel> {
    fun create(handle: SavedStateHandle): T
}