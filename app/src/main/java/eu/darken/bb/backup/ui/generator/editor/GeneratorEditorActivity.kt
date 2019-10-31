package eu.darken.bb.backup.ui.generator.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.backup.core.putGeneratorId
import eu.darken.bb.common.navigation.isGraphSet
import eu.darken.bb.common.observe2
import eu.darken.bb.common.smart.SmartActivity
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

class GeneratorEditorActivity : SmartActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    @Inject lateinit var vdcSource: VDCSource.Factory
    private val vdc: GeneratorEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorEditorActivityVDC.Factory
        factory.create(handle, intent.getGeneratorId()!!)
    })

    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.generator_editor_activity)
        ButterKnife.bind(this)

        vdc.state.observe2(this) { state ->
            if (state.isExisting) {
                supportActionBar!!.title = getString(R.string.label_edit_source_config)
            } else {
                supportActionBar!!.title = getString(R.string.label_create_source_config)
            }

            if (!navController.isGraphSet()) {
                val graph = navController.navInflater.inflate(R.navigation.source_editor)
                graph.startDestination = state.stepFlow.start
                navController.setGraph(graph, Bundle().apply { putGeneratorId(state.generatorId) })
                setupActionBarWithNavController(navController)
            }

            allowCreate = state.isValid
            existing = state.isExisting
            invalidateOptionsMenu()

            loadingOverlay.setGone(!state.isWorking)
        }

        vdc.finishEvent.observe2(this) { finish() }

        onBackPressedDispatcher.addCallback {
            if (!navController.popBackStack()) finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_generator_editor_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_create).isVisible = allowCreate
        menu.findItem(R.id.action_create).title = getString(if (existing) R.string.action_save else R.string.action_create)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_create -> {
            vdc.saveConfig()
            true
        }
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
