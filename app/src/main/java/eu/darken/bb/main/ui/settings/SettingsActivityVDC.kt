package eu.darken.bb.main.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.common.SmartVDC
import eu.darken.bb.common.dagger.SavedStateVDCFactory


class SettingsActivityVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<SettingsActivityVDC>
}