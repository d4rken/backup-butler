package eu.darken.bb.common.vdc

import androidx.lifecycle.SavedStateHandle

interface SavedStateVDCFactory<T : VDC> : VDCFactory<T> {
    fun create(handle: SavedStateHandle): T
}