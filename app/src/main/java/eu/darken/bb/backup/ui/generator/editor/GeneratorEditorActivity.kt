package eu.darken.bb.backup.ui.generator.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import eu.darken.bb.R
import eu.darken.bb.backup.core.getGeneratorId
import eu.darken.bb.backup.core.putGeneratorId
import eu.darken.bb.common.showFragment
import eu.darken.bb.common.ui.LoadingOverlayView
import eu.darken.bb.common.ui.setGone
import eu.darken.bb.common.vdc.VDCSource
import eu.darken.bb.common.vdc.vdcsAssisted
import javax.inject.Inject

class GeneratorEditorActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var vdcSource: VDCSource.Factory

    private val vdc: GeneratorEditorActivityVDC by vdcsAssisted({ vdcSource }, { factory, handle ->
        factory as GeneratorEditorActivityVDC.Factory
        factory.create(handle, intent.getGeneratorId()!!)
    })

    @BindView(R.id.loading_overlay) lateinit var loadingOverlay: LoadingOverlayView

    private var allowCreate: Boolean = false
    private var existing: Boolean = false

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> = dispatchingAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.generator_editor_activity)
        ButterKnife.bind(this)

        vdc.state.observe(this, Observer { state ->
            if (state.existing) {
                supportActionBar!!.title = getString(R.string.label_edit_source_config)
            } else {
                supportActionBar!!.title = getString(R.string.label_create_source_config)
            }

            allowCreate = state.allowSave
            existing = state.existing
            invalidateOptionsMenu()

            loadingOverlay.setGone(!state.working)
        })

        vdc.pageEvent.observe(this, Observer { pageEvent ->
            showFragment(
                    fragmentClass = pageEvent.getPage().fragmentClass,
                    arguments = Bundle().apply { putGeneratorId(pageEvent.generatorId) }
            )
        })

        vdc.finishActivity.observe(this, Observer { finish() })
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (supportFragmentManager.popBackStackImmediate()) {
            true
        } else {
            finish()
            super.onSupportNavigateUp()
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
        else -> super.onOptionsItemSelected(item)
    }
}
