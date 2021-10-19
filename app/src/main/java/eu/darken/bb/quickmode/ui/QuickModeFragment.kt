package eu.darken.bb.quickmode.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.logging.log
import eu.darken.bb.common.lists.setupDefaults
import eu.darken.bb.common.lists.update
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.QuickmodeMainFragmentBinding
import eu.darken.bb.settings.ui.SettingsActivity
import javax.inject.Inject

@AndroidEntryPoint
class QuickModeFragment : SmartFragment(R.layout.quickmode_main_fragment) {

    private val vdc: QuickModeFragmentVDC by viewModels()
    private val ui: QuickmodeMainFragmentBinding by viewBinding()
    @Inject lateinit var adapter: QuickModeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.apply {
            recyclerView.setupDefaults(adapter, dividers = false)
        }

        vdc.navEvents.observe2(this) { doNavigate(it) }

        vdc.debugState.observe2(this, ui) { state ->
            toolbar.menu.apply {
                findItem(R.id.action_record_debuglog).apply {
                    isVisible = state.showDebugStuff
                    isEnabled = !state.isRecordingDebug
                }
                findItem(R.id.action_report_bug).isVisible = state.showDebugStuff
            }
        }

        vdc.items.observe2(this) {
            log { "Updating adapter with $it" }
            adapter.update(it)
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
