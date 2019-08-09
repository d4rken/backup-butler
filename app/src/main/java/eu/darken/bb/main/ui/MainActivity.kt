package eu.darken.bb.main.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.dagger.VDCSource
import eu.darken.bb.common.vdcs
import eu.darken.bb.settings.ui.SettingsActivity
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector, AutoInject {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: MainActivityVDC by vdcs { vdcSource }

    @BindView(R.id.viewpager) lateinit var viewPager: ViewPager2
    @BindView(R.id.tablayout) lateinit var tabLayout: TabLayout

    @Inject lateinit var pagerPages: List<PagerAdapter.Page>
    private val pagerAdapter by lazy { PagerAdapter(this, pagerPages) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.BaseAppTheme)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        ButterKnife.bind(this)

        viewPager.adapter = pagerAdapter

        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        TabLayoutMediator(tabLayout, viewPager, TabLayoutMediator.OnConfigureTabCallback { tab, position ->
            tab.setText(pagerAdapter.pages[position].titleRes)
        }).attach()

        vdc.state.observe(this, Observer { if (it.ready) showExampleFragment() })

        vdc.onGo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun showExampleFragment() {
//        var fragment = supportFragmentManager.findFragmentById(R.versionId.content_frame)
//        if (fragment == null) fragment = OverviewFragment.newInstance()
//        supportFragmentManager.beginTransaction().replace(R.versionId.content_frame, fragment).commitAllowingStateLoss()
    }
}
