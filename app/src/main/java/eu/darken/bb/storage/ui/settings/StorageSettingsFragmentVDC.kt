package eu.darken.bb.storage.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC

class StorageSettingsFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<StorageSettingsFragmentVDC>
}