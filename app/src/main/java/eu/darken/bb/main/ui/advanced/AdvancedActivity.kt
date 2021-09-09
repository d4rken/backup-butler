package eu.darken.bb.main.ui.advanced

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.core.UISettings
import eu.darken.bb.main.ui.advanced.debug.DebugFragment
import eu.darken.bb.main.ui.settings.SettingsActivity
import javax.inject.Inject

@AndroidEntryPoint
class AdvancedActivity : AppCompatActivity() {

//    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
//    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: AdvancedActivityVDC by vdcs { vdcSource }

    @BindView(R.id.viewpager) lateinit var viewPager: ViewPager2
    @BindView(R.id.tablayout) lateinit var tabLayout: TabLayout

    @Inject lateinit var uiSettings: UISettings
    @Inject lateinit var pagerPages: List<PagerAdapter.Page>
    private lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.AppTheme_Base)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_advanced_activity)
        ButterKnife.bind(this)

        var pages = pagerPages
        if (uiSettings.showDebugPage) {
            pages = pages.plus(PagerAdapter.Page(DebugFragment::class, R.string.debug_label))
        }
        pagerAdapter = PagerAdapter(this, pages)

        viewPager.adapter = pagerAdapter

        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        TabLayoutMediator(tabLayout, viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            tab.setText(pagerAdapter.pages[position].titleRes)
        }).attach()

        vdc.onGo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_advanced, menu)
        return true
    }

    @Inject lateinit var debug: BBDebug
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
