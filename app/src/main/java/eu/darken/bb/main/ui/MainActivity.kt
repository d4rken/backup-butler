package eu.darken.bb.main.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.androidkotlinstarter.common.dagger.VDCSource
import eu.darken.bb.R
import eu.darken.bb.main.ui.overview.OverviewFragment
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory
    private val viewModel: MainActivityViewModel by viewModels { vdcSource.create(this, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.BaseAppTheme_NoActionBar)
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        ButterKnife.bind(this)

        viewModel.state.observe(this, Observer { if (it.ready) showExampleFragment() })

        viewModel.onGo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    fun showExampleFragment() {
        var fragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (fragment == null) fragment = OverviewFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss()
    }
}
