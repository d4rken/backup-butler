package eu.darken.bb.main.ui.start.debug

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC

class DebugFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        @AppContext private val context: Context
) : SmartVDC() {


    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<DebugFragmentVDC>
}