package eu.darken.bb.common.dagger

import androidx.lifecycle.SavedStateHandle
import eu.darken.bb.common.VDC

interface SavedStateVDCFactory<T : VDC> : VDCFactory<T> {
    fun create(handle: SavedStateHandle): T
}