package eu.darken.bb.main.ui.advanced.overview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.viewModels
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.observe2
import eu.darken.bb.common.rx.clicksDebounced
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.user.core.UpgradeData

@AndroidEntryPoint
class OverviewFragment : SmartFragment() {

    @BindView(R.id.card_appinfos_version) lateinit var appVersion: TextView
    @BindView(R.id.card_appinfos_upgrades) lateinit var upgradeInfos: TextView

    @BindView(R.id.card_update) lateinit var updateCard: View
    @BindView(R.id.card_update_action_changelog) lateinit var changelogAction: Button
    @BindView(R.id.card_update_action_update) lateinit var updateAction: Button

    private val vdc: OverviewFragmentVDC by viewModels()

    init {
        layoutRes = R.layout.overview_fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.appState.observe2(this) {
            @SuppressLint("SetTextI18n")
            appVersion.text =
                "v${it.appInfo.versionName}(${it.appInfo.versionCode}) [${it.appInfo.buildState} ${it.appInfo.gitSha} ${it.appInfo.buildTime}]"
            upgradeInfos.text = when {
                it.upgradeData.state == UpgradeData.State.PRO -> getString(R.string.upgrade_proversion_label)
                else -> getString(R.string.upgrade_basicversion_label)
            }
        }

        vdc.updateState.observe2(this) { updateState ->
            updateCard.setGone(!updateState.available)
        }
        changelogAction.clicksDebounced().subscribe { vdc.onChangelog() }
        updateAction.clicksDebounced().subscribe { vdc.onUpdate() }

        super.onViewCreated(view, savedInstanceState)
    }

}
