package eu.darken.bb.common.dagger

import androidx.lifecycle.SavedStateHandle
import eu.darken.bb.common.VDC

interface SavedStateVDCFactory<T : VDC> {
    fun create(handle: SavedStateHandle): T
}