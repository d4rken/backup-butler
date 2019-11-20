package eu.darken.bb.main.ui.simple

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC


class SimpleActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : VDC() {

    data class State(val ready: Boolean)

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<SimpleActivityVDC>
}