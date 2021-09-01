package eu.darken.bb.main.ui.advanced.debug

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC

class DebugFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @AppContext private val context: Context
) : SmartVDC() {


    @AssistedFactory
    interface Factory : SavedStateVDCFactory<DebugFragmentVDC>
}