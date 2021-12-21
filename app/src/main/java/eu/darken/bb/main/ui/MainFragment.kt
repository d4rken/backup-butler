package eu.darken.bb.main.ui

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.colorString
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.smart.Smart2Fragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.MainFragmentBinding
import eu.darken.bb.settings.ui.SettingsActivity

@AndroidEntryPoint
class MainFragment : Smart2Fragment(R.layout.main_fragment) {
    override val vdc: MainFragmentVDC by viewModels()
    override val ui: MainFragmentBinding by viewBinding()

    lateinit var adapter: MainPagerAdapter

    fun Toolbar.updateTitle(isPro: Boolean) {
        if (!isPro) {
            title = getString(R.string.app_name)
            return
        }

        val split = getString(R.string.app_name_pro)
            .split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        title = if (split.size == 3) {
            val builder = SpannableStringBuilder(split[0] + " " + split[1] + " ")
            builder.append(colorString(context, R.color.colorSecondary, split[2]))
            builder
        } else {
            getString(R.string.app_name)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vdc.state.observe2(ui) { state ->
            adapter = MainPagerAdapter(childFragmentManager, lifecycle, state.pages)

            viewpager.adapter = adapter
            tablayout.tabMode = TabLayout.MODE_SCROLLABLE
            // When smoothScroll is enabled and we navigate to an unloaded fragment, ??? happens we jump to the wrong position
            TabLayoutMediator(tablayout, viewpager, true, false) { tab, position ->
                tab.setText(adapter.pages[position].titleRes)
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

        vdc.appTitle.observe2(ui) { (isPro, appInfo) ->
            toolbar.updateTitle(isPro)
            toolbar.subtitle = if (BBDebug.isDebug()) {
                "v${appInfo.fullVersionString}"
            } else {
                "v${appInfo.versionName}"
            }
        }

        ui.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                vdc.updateCurrentPage(position)
            }
        })

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
                    vdc.switchUIMode()
                    true
                }
                else -> false
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

}
