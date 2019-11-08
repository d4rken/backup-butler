package eu.darken.bb.storage.ui.viewer

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

class StorageViewerActivity : AppCompatActivity(), HasSupportFragmentInjector {

    val navArgs by navArgs<StorageViewerActivityArgs>()

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    val vdc: StorageViewerActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as StorageViewerActivityVDC.Factory
        factory.create(handle, navArgs.storageId)
    })

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val graph by lazy { navController.navInflater.inflate(R.navigation.storage_viewer) }
    private val appBarConf by lazy {
        AppBarConfiguration.Builder()
                .setFallbackOnNavigateUpListener {
                    finish()
                    true
                }
                .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.storage_viewer_activity)
        ButterKnife.bind(this)

        vdc.errorEvent.observe2(this) {
            Toast.makeText(this, it.tryLocalizedErrorMessage(this), Toast.LENGTH_LONG).show()
        }

        vdc.state.observe2(this) { state ->
            if (!navController.isGraphSet()) {
                navController.setGraph(graph, bundleOf("storageId" to state.storageId))
                setupActionBarWithNavController(navController, appBarConf)
            }
        }

        vdc.finishActivity.observe(this, Observer { finish() })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConf) || super.onSupportNavigateUp()
    }
}
