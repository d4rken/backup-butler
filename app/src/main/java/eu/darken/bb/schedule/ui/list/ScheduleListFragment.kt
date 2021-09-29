package eu.darken.bb.schedule.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.ScheduleListFragmentBinding

@AndroidEntryPoint
class ScheduleListFragment : SmartFragment(R.layout.schedule_list_fragment) {

    private val vdc: SchedulesFragmentVDC by viewModels()
    private val ui: ScheduleListFragmentBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.scheduleData.observe2(this, ui) { state ->
            scheduleListWrapper.updateLoadingState(state.isLoading)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
