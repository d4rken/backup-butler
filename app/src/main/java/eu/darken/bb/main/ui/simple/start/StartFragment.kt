package eu.darken.bb.main.ui.simple.start

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import butterknife.BindView
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.smart.SmartFragment

@AndroidEntryPoint
class StartFragment : SmartFragment() {
    companion object {
        fun newInstance(): Fragment = StartFragment()
    }

    @BindView(R.id.card_appinfos_version) lateinit var appVersion: TextView
    @BindView(R.id.card_appinfos_upgrades) lateinit var upgradeInfos: TextView

    @BindView(R.id.card_update) lateinit var updateCard: View
    @BindView(R.id.card_update_action_changelog) lateinit var changelogAction: Button
    @BindView(R.id.card_update_action_update) lateinit var updateAction: Button

    private val vdc: StartFragmentVDC by viewModels()

    init {
        layoutRes = R.layout.start_fragment
    }

}
