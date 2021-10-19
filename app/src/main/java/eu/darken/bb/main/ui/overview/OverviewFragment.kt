package eu.darken.bb.main.ui.overview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OverviewFragmentBinding
import eu.darken.bb.user.core.UpgradeData

@AndroidEntryPoint
class OverviewFragment : SmartFragment(R.layout.overview_fragment) {

    private val vdc: OverviewFragmentVDC by viewModels()
    private val ui: OverviewFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.appState.observe2(this, ui) {
            @SuppressLint("SetTextI18n")
            cardAppinfosVersion.text =
                "v${it.appInfo.versionName}(${it.appInfo.versionCode}) [${it.appInfo.buildState} ${it.appInfo.gitSha} ${it.appInfo.buildTime}]"
            cardAppinfosUpgrades.text = when {
                it.upgradeData.state == UpgradeData.State.PRO -> getString(R.string.upgrade_proversion_label)
                else -> getString(R.string.upgrade_basicversion_label)
            }
        }

        vdc.updateState.observe2(this, ui) { updateState ->
            cardUpdate.setGone(!updateState.available)
        }
        ui.cardUpdateActionChangelog.clicksDebounced().subscribe { vdc.onChangelog() }
        ui.cardUpdateActionUpdate.clicksDebounced().subscribe { vdc.onUpdate() }

        super.onViewCreated(view, savedInstanceState)
    }

}
