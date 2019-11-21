package eu.darken.bb.main.ui.settings.support

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC

class SupportFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<SupportFragmentVDC>

}