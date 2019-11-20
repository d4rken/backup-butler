package eu.darken.bb.main.ui.simple

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.dagger.AutoInject
import eu.darken.bb.common.debug.BBDebug
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcs
import eu.darken.bb.main.ui.settings.SettingsActivity
import javax.inject.Inject


class SimpleActivity : AppCompatActivity(), HasSupportFragmentInjector, AutoInject {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: SimpleActivityVDC by vdcs { vdcSource }

    @BindView(R.id.viewpager) lateinit var viewPager: ViewPager2
    @BindView(R.id.tablayout) lateinit var tabLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Base)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_simple_activity)
        ButterKnife.bind(this)

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
