package eu.darken.bb.main.ui.simple

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.MainSimpleModeFragmentBinding
import eu.darken.bb.main.ui.settings.SettingsActivity

@AndroidEntryPoint
class SimpleModeFragment : SmartFragment(R.layout.main_simple_mode_fragment) {

    private val vdc: SimpleModeFragmentVDC by viewModels()
    private val ui: MainSimpleModeFragmentBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        requireActivityActionBar().subtitle = getString(R.string.startmode_simple_label)

        ui.apply {
//            appVersion
//            upgradeInfos
//
//            updateCard
//            changelogAction
//            updateAction
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }

        vdc.state.observe2(this, ui) { state ->
            toolbar.menu.apply {
                findItem(R.id.action_record_debuglog).apply {
                    isVisible = state.showDebugStuff
                    isEnabled = !state.isRecordingDebug
                }
                findItem(R.id.action_report_bug).isVisible = state.showDebugStuff
            }
        }

        ui.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                    true
                }
                R.id.action_record_debuglog -> {
                    vdc.recordDebugLog()
                    true
                }
                R.id.action_report_bug -> {
                    vdc.reportBug()
                    true
                }
                R.id.action_switch_startmode -> {
                    vdc.switchToAdvancedMode()
                    true
                }
                else -> false
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
