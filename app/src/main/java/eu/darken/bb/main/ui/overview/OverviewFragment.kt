package eu.darken.bb.main.ui.overview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import com.jakewharton.rxbinding3.view.clicks
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.vdcs
import eu.darken.bb.upgrades.UpgradeData
import javax.inject.Inject


class OverviewFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = OverviewFragment()
    }

    @BindView(R.id.card_appinfos_version) lateinit var appVersion: TextView
    @BindView(R.id.card_appinfos_upgrades) lateinit var upgradeInfos: TextView
    @BindView(R.id.card_debug_testbutton) lateinit var testButton: Button

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: OverviewFragmentVDC by vdcs { vdcSource }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.overview_fragment, container, false)
        addUnbinder(ButterKnife.bind(this, layout))
        return layout
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        testButton.clicks().subscribe { vdc.test() }

        vdc.appState.observe(this, Observer {
            appVersion.text = "v${it.appInfo.versionName}(${it.appInfo.versionCode}) [${it.appInfo.buildState} ${it.appInfo.gitSha} ${it.appInfo.buildTime}]"
            upgradeInfos.text = when {
                it.upgradeData.state == UpgradeData.State.PRO -> getString(R.string.label_pro_version)
                else -> getString(R.string.label_basic_version)
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }

}
