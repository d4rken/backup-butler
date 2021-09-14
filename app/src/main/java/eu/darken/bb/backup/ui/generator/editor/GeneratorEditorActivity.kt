package eu.darken.bb.backup.ui.generator.editor

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import butterknife.ButterKnife
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.bb.R
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartActivity

@AndroidEntryPoint
class GeneratorEditorActivity : SmartActivity() {

    val navArgs by navArgs<GeneratorEditorActivityArgs>()

    private val vdc: GeneratorEditorActivityVDC by viewModels()

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.generator_editor_activity)
        ButterKnife.bind(this)

        vdc.state.observe2(this) { state ->
            supportActionBar?.subtitle = if (state.isExisting) getString(R.string.backup_edit_source_label)
            else getString(R.string.backup_create_source_label)

            if (!navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.generator_editor)
                graph.startDestination = state.stepFlow.start
                navController.setGraph(graph, bundleOf("generatorId" to state.generatorId))

                setupActionBarWithNavController(navController)
            }
        }

        vdc.finishEvent.observe2(this) { finish() }

        onBackPressedDispatcher.addCallback {
            if (!navController.popBackStack()) finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        if (isFinishing) vdc.dismiss()
        super.onDestroy()
    }
}
