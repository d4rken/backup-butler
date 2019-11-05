package eu.darken.bb.schedule.ui.list

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import eu.darken.bb.BackupButler
import eu.darken.bb.common.dagger.AppContext
import eu.darken.bb.common.vdc.SavedStateVDCFactory
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.service.ProcessorService
import eu.darken.bb.user.core.UpgradeControl
import eu.darken.bb.user.core.UpgradeData

class SchedulesFragmentVDC @AssistedInject constructor(
        @Assisted private val handle: SavedStateHandle,
        private val butler: BackupButler,
        private val upgradeControl: UpgradeControl,
        @AppContext private val context: Context
) : SmartVDC() {


    fun test() {
        context.startService(Intent(context, ProcessorService::class.java))
    }

    data class AppState(
            val appInfo: BackupButler.AppInfo,
            val upgradeData: UpgradeData
    )

    @AssistedInject.Factory
    interface Factory : SavedStateVDCFactory<SchedulesFragmentVDC>
}