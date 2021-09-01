package eu.darken.bb.backup.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC

class BackupSettingsFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle
) : SmartVDC() {

    @AssistedFactory
    interface Factory : SavedStateVDCFactory<BackupSettingsFragmentVDC>
}