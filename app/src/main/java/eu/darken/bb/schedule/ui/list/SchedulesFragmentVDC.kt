package eu.darken.bb.schedule.ui.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.vdc.SmartVDC
import javax.inject.Inject

@HiltViewModel
class SchedulesFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
) : SmartVDC() {

    val scheduleData = MutableLiveData(ScheduleState())

    data class ScheduleState(
        val isLoading: Boolean = false
    )
}