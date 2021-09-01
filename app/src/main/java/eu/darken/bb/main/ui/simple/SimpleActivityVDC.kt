package eu.darken.bb.main.ui.simple

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.VDC


class SimpleActivityVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : VDC() {

    data class State(val ready: Boolean)

    @AssistedFactory
    interface Factory : SavedStateVDCFactory<SimpleActivityVDC>
}