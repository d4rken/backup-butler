package eu.darken.bb.main.ui.advanced

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.doNavigate
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.MainAdvancedModeFragmentBinding
import eu.darken.bb.main.ui.settings.SettingsActivity

@AndroidEntryPoint
class AdvancedModeFragment : SmartFragment(R.layout.main_advanced_mode_fragment) {
    val vdc: AdvancedModeFragmentVDC by viewModels()
    val ui: MainAdvancedModeFragmentBinding by viewBinding()

    lateinit var pagerAdapter: PagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(this, ui) { state ->
            pagerAdapter = PagerAdapter(childFragmentManager, lifecycle, state.pages)

            viewpager.adapter = pagerAdapter
            tablayout.tabMode = TabLayout.MODE_SCROLLABLE
            // When smoothScroll is enabled and we navigate to an unloaded fragment, ??? happens we jump to the wrong position
            TabLayoutMediator(tablayout, viewpager, true, false) { tab, position ->
                tab.setText(pagerAdapter.pages[position].titleRes)
            }.attach()

            toolbar.menu.apply {
                findItem(R.id.action_record_debuglog).apply {
                    isVisible = state.showDebugStuff
                    isEnabled = !state.isRecordingDebug
                }
                findItem(R.id.action_report_bug).isVisible = state.showDebugStuff
            }

            viewpager.setCurrentItem(state.pagePosition, false)
        }
        ui.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                vdc.updateCurrentPage(position)
            }
        })

        vdc.navEvents.observe2(this) { doNavigate(it) }

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
                    vdc.switchToSimpleMode()
                    true
                }
                else -> false
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

}
