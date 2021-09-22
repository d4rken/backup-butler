package eu.darken.bb.main.ui.advanced

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.smart.SmartFragment
import eu.darken.bb.common.viewBinding
import eu.darken.bb.databinding.MainAdvancedModeFragmentBinding
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.advanced.debug.DebugFragment
import eu.darken.bb.main.ui.settings.SettingsActivity
import javax.inject.Inject

@AndroidEntryPoint
class AdvancedModeFragment : SmartFragment(R.layout.main_advanced_mode_fragment) {
    private val vdc: AdvancedActivityVDC by viewModels()
    private val binding: MainAdvancedModeFragmentBinding by viewBinding()

    @Inject lateinit var uiSettings: UISettings
    @Inject lateinit var pagerPages: List<PagerAdapter.Page>
    private lateinit var pagerAdapter: PagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pages = pagerPages
        if (uiSettings.showDebugPage) {
            pages = pages.plus(PagerAdapter.Page(DebugFragment::class, R.string.debug_label))
        }
        pagerAdapter = PagerAdapter(requireActivity(), pages)

        binding.apply {
            viewpager.adapter = pagerAdapter
            tablayout.tabMode = TabLayout.MODE_SCROLLABLE
            TabLayoutMediator(tablayout, viewpager) { tab, position ->
                tab.setText(pagerAdapter.pages[position].titleRes)
            }.attach()

        }

        vdc.onGo()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main_advanced, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Inject lateinit var debug: BBDebug
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
