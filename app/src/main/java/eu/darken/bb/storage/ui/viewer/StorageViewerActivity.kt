package eu.darken.bb.storage.ui.viewer

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.tryLocalizedErrorMessage
import eu.darken.bb.databinding.StorageViewerActivityBinding

@AndroidEntryPoint
class StorageViewerActivity : AppCompatActivity() {

    val navArgs by navArgs<StorageViewerActivityArgs>()

    val vdc: StorageViewerActivityVDC by viewModels()
    private lateinit var ui: StorageViewerActivityBinding

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
        super.onCreate(savedInstanceState)

        ui = StorageViewerActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        vdc.errorEvent.observe2(this) {
            Toast.makeText(this, it.tryLocalizedErrorMessage(this), Toast.LENGTH_LONG).show()
        }

        vdc.state.observe2(this) { state ->
            if (!navController.isGraphSet()) {
                navController.setGraph(graph, bundleOf("storageId" to state.storageId))
                setupActionBarWithNavController(navController, appBarConf)
            }
        }

        vdc.finishActivity.observe2(this) { finish() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConf) || super.onSupportNavigateUp()
    }
}
