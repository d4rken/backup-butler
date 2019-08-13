package eu.darken.bb.main.ui.start.overview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.vdcs
import eu.darken.bb.upgrades.UpgradeData
import javax.inject.Inject


class OverviewFragment : SmartFragment(), AutoInject {
    companion object {
        fun newInstance(): Fragment = OverviewFragment()
    }

    @BindView(R.id.card_appinfos_version) lateinit var appVersion: TextView
    @BindView(R.id.card_appinfos_upgrades) lateinit var upgradeInfos: TextView

    @BindView(R.id.card_update) lateinit var updateCard: View
    @BindView(R.id.card_update_action_changelog) lateinit var changelogAction: Button
    @BindView(R.id.card_update_action_update) lateinit var updateAction: Button

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: OverviewFragmentVDC by vdcs { vdcSource }

    init {
        layoutRes = R.layout.overview_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.appState.observe(this, Observer {
            @SuppressLint("CheckResult", "SetTextI18n")
            appVersion.text = "v${it.appInfo.versionName}(${it.appInfo.versionCode}) [${it.appInfo.buildState} ${it.appInfo.gitSha} ${it.appInfo.buildTime}]"
            upgradeInfos.text = when {
                it.upgradeData.state == UpgradeData.State.PRO -> getString(R.string.label_pro_version)
                else -> getString(R.string.label_basic_version)
            }
        })

        vdc.updateState.observe(this, Observer { updateState ->
            updateCard.setGone(!updateState.available)
        })
        changelogAction.clicksDebounced().subscribe { vdc.onChangelog() }
        updateAction.clicksDebounced().subscribe { vdc.onUpdate() }

        super.onViewCreated(view, savedInstanceState)
    }

}
