package eu.darken.bb.trigger.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.ScheduleListFragmentBinding

@AndroidEntryPoint
class TriggerListFragment : Smart2Fragment(R.layout.schedule_list_fragment) {

    override val vdc: TriggerFragmentVDC by viewModels()
    override val ui: ScheduleListFragmentBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.scheduleData.observe2(ui) { state ->
            scheduleListWrapper.updateLoadingState(state.isLoading)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}
