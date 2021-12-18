package eu.darken.bb.main.ui.overview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.OverviewFragmentBinding
import eu.darken.bb.user.core.UpgradeInfo

@AndroidEntryPoint
class OverviewFragment : Smart2Fragment(R.layout.overview_fragment) {

    override val vdc: OverviewFragmentVDC by viewModels()
    override val ui: OverviewFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.appState.observe2(ui) {
            @SuppressLint("SetTextI18n")
            cardAppinfosVersion.text =
                "v${it.appInfo.versionName}(${it.appInfo.versionCode}) [${it.appInfo.buildState} ${it.appInfo.gitSha} ${it.appInfo.buildTime}]"
            cardAppinfosUpgrades.text = when {
                it.upgradeInfo.state == UpgradeInfo.State.PRO -> getString(R.string.upgrade_proversion_label)
                else -> getString(R.string.upgrade_basicversion_label)
            }
        }

        vdc.updateState.observe2(ui) { updateState ->
            cardUpdate.setGone(!updateState.available)
        }
        ui.cardUpdateActionChangelog.setOnClickListener { vdc.onChangelog() }
        ui.cardUpdateActionUpdate.setOnClickListener { vdc.onUpdate() }

        super.onViewCreated(view, savedInstanceState)
    }

}
