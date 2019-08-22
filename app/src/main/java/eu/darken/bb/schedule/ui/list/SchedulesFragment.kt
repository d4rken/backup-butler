package eu.darken.bb.schedule.ui.list

import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject


class SchedulesFragment : SmartFragment(), AutoInject {

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SchedulesFragmentVDC by vdcs { vdcSource }

    init {
        layoutRes = R.layout.schedule_list_fragment
    }

}
