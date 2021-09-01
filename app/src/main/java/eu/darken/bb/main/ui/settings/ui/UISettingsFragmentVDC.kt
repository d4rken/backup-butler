package eu.darken.bb.main.ui.settings.ui

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC

class UISettingsFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    @AssistedFactory
    interface Factory : SavedStateVDCFactory<UISettingsFragmentVDC>
}