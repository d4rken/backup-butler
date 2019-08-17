package eu.darken.bb.schedule.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import javax.inject.Inject


class SchedulesFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = SchedulesFragment()
    }

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SchedulesFragmentVDC by vdcs { vdcSource }

    init {
        layoutRes = R.layout.schedule_list_fragment
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
    }

}
