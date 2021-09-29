package eu.darken.bb.backup.ui.generator.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.viewModels
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
import eu.darken.bb.common.requireActionBar
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.databinding.GeneratorEditorActivityBinding

@AndroidEntryPoint
class GeneratorEditorActivity : SmartActivity() {

    val navArgs by navArgs<GeneratorEditorActivityArgs>()
    lateinit var ui: GeneratorEditorActivityBinding
    val vdc: GeneratorEditorActivityVDC by viewModels()

    private val navController by lazy { findNavController(R.id.nav_host) }
    private val appBarConf = AppBarConfiguration.Builder().apply {
        setFallbackOnNavigateUpListener {
            finish()
            true
        }
    }.build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = GeneratorEditorActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)

        vdc.state.observe2(this) { state ->

            if (!navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.generator_editor)
                graph.startDestination = state.stepFlow.start
                navController.setGraph(graph, bundleOf("generatorId" to state.generatorId))
                setupActionBarWithNavController(navController)

                navController.addOnDestinationChangedListener { controller, destination, _ ->
                    requireActionBar().apply {
                        title = if (state.isExisting) getString(R.string.backup_edit_source_label)
                        else getString(R.string.backup_create_source_label)
                        subtitle = destination.label
                    }
                    @SuppressLint("RestrictedApi")
                    if (controller.backStack.size <= 2) {
                        requireActionBar().setDisplayHomeAsUpEnabled(true)
                        requireActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
                    }
                }
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
        return NavigationUI.navigateUp(navController, appBarConf) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        if (isFinishing) vdc.dismiss()
        super.onDestroy()
    }
}
