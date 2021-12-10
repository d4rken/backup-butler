package eu.darken.bb.schedule.ui.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.bb.common.coroutine.DispatcherProvider
import eu.darken.bb.common.smart.Smart2VDC
import javax.inject.Inject

@HiltViewModel
class TriggerFragmentVDC @Inject constructor(
    private val handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
) : Smart2VDC(dispatcherProvider) {

    val scheduleData = MutableLiveData(ScheduleState())

    data class ScheduleState(
        val isLoading: Boolean = false
    )
}