package eu.darken.bb.schedule.ui.list

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.bb.BackupButler
import eu.darken.bb.common.vdc.SmartVDC
import eu.darken.bb.processor.core.service.ProcessorService
import eu.darken.bb.user.core.UpgradeControl
import eu.darken.bb.user.core.UpgradeData
import javax.inject.Inject

@HiltViewModel
class SchedulesFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val butler: BackupButler,
    private val upgradeControl: UpgradeControl,
    @ApplicationContext private val context: Context
) : SmartVDC() {


    fun test() {
        context.startService(Intent(context, ProcessorService::class.java))
    }

    data class AppState(
        val appInfo: BackupButler.AppInfo,
        val upgradeData: UpgradeData
    )
}