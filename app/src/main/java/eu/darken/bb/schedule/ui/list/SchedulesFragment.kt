package eu.darken.bb.schedule.ui.list

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment

@AndroidEntryPoint
class SchedulesFragment : SmartFragment(R.layout.schedule_list_fragment) {

    private val vdc: SchedulesFragmentVDC by viewModels()

}
