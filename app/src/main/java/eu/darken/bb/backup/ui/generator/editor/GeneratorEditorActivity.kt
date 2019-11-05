package eu.darken.bb.backup.ui.generator.editor

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

class GeneratorEditorActivity : SmartActivity(), HasSupportFragmentInjector {

    val navArgs by navArgs<GeneratorEditorActivityArgs>()

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneratorEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorEditorActivityVDC.Factory
        factory.create(handle, navArgs.generatorId)
    })

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
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
